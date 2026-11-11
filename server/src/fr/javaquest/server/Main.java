package fr.javaquest.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.javaquest.common.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Serveur local JavaQuest : 127.0.0.1 uniquement, jeton de session obligatoire pour l'API,
 * contrôle de l'en-tête Host (anti DNS-rebinding), fichiers statiques avec CSP stricte.
 * Affiche sur stdout : {@code JAVAQUEST_READY http://127.0.0.1:<port>/#t=<jeton>}.
 */
public final class Main {
    public static final String VERSION = "2.0.0-dev";
    private static final Map<String, String> MIME = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"), Map.entry("js", "text/javascript; charset=utf-8"),
            Map.entry("mjs", "text/javascript; charset=utf-8"), Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("json", "application/json; charset=utf-8"), Map.entry("png", "image/png"),
            Map.entry("svg", "image/svg+xml"), Map.entry("ogg", "audio/ogg"), Map.entry("wav", "audio/wav"),
            Map.entry("mp3", "audio/mpeg"), Map.entry("woff2", "font/woff2"), Map.entry("ttf", "font/ttf"),
            Map.entry("txt", "text/plain; charset=utf-8"), Map.entry("md", "text/plain; charset=utf-8"));
    private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data: blob:; media-src 'self' data: blob:; font-src 'self'; connect-src 'self'; "
            + "worker-src 'self' blob:; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'none'";

    private final Path web;
    private final Content content;
    private final Storage storage;
    private final SandboxRunner runner;
    private final String token;
    private int port;

    private Main(Path web, Content content, Storage storage, SandboxRunner runner, String token) {
        this.web = web; this.content = content; this.storage = storage; this.runner = runner; this.token = token;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> o = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) o.put(args[i].replaceFirst("^--", ""), args[i + 1]);
        Path app = Path.of(o.getOrDefault("app", ".")).toAbsolutePath().normalize();
        Path web = Path.of(o.getOrDefault("web", app.resolve("web").toString())).toAbsolutePath().normalize();
        Path contentDir = Path.of(o.getOrDefault("content", app.resolve("content").toString())).toAbsolutePath();
        Path runtime = Path.of(o.getOrDefault("runtime", app.resolve("runtime").toString())).toAbsolutePath();
        Path sandbox = Path.of(o.getOrDefault("sandbox", app.resolve("bin/jq-sandbox.exe").toString())).toAbsolutePath();
        Path runnerJar = Path.of(o.getOrDefault("runner", app.resolve("runner/runner.jar").toString())).toAbsolutePath();
        String local = System.getenv("LOCALAPPDATA");
        Path data = Path.of(o.getOrDefault("data", (local != null ? Path.of(local) : Path.of(System.getProperty("user.home"))).resolve("JavaQuest").toString())).toAbsolutePath();
        int wanted = Integer.parseInt(o.getOrDefault("port", "0"));

        Files.createDirectories(data);
        SandboxRunner runner = new SandboxRunner(sandbox, runtime, runnerJar);
        runner.prepare(runtime, data);
        byte[] t = new byte[24];
        new SecureRandom().nextBytes(t);
        Main m = new Main(web, new Content(contentDir), new Storage(data), runner, HexFormat.of().formatHex(t));
        HttpServer server = m.bind(wanted);
        server.setExecutor(Executors.newFixedThreadPool(6));
        server.createContext("/", m::handle);
        server.start();
        System.out.println("JAVAQUEST_READY http://127.0.0.1:" + m.port + "/#t=" + m.token);
        System.out.flush();
        if ("1".equals(o.get("watch-stdin"))) {
            // Le lanceur garde stdin ouvert : sa fermeture (lanceur arrêté) arrête le serveur.
            Thread w = new Thread(() -> {
                try { while (System.in.read() >= 0) { } } catch (IOException ignored) { }
                server.stop(0);
                System.exit(0);
            }, "stdin-watch");
            w.setDaemon(true);
            w.start();
        }
    }

    private HttpServer bind(int wanted) throws IOException {
        InetAddress lo = InetAddress.getByName("127.0.0.1");
        List<Integer> candidates = new ArrayList<>();
        if (wanted > 0) candidates.add(wanted);
        for (int p = 8790; p < 8810; p++) candidates.add(p);
        candidates.add(0);
        for (int p : candidates) {
            try {
                HttpServer s = HttpServer.create(new InetSocketAddress(lo, p), 64);
                port = s.getAddress().getPort();
                return s;
            } catch (BindException ignored) { }
        }
        throw new BindException("aucun port local disponible");
    }

    private void handle(HttpExchange ex) throws IOException {
        try {
            Map<String, List<String>> h = ex.getRequestHeaders();
            String host = ex.getRequestHeaders().getFirst("Host");
            if (host == null || !(host.equals("127.0.0.1:" + port) || host.equals("localhost:" + port))) {
                send(ex, 421, "text/plain; charset=utf-8", "Hôte refusé".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String path = ex.getRequestURI().getPath();
            if (path.startsWith("/api/")) api(ex, path);
            else serveStatic(ex, path);
        } catch (IllegalArgumentException e) {
            try { json(ex, 400, Map.of("error", e.getMessage())); } catch (IOException ignored) { }
        } catch (Exception e) {
            try { json(ex, 500, Map.of("error", "Erreur interne : " + e.getMessage())); } catch (IOException ignored) { }
        } finally {
            ex.close();
        }
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        if (!ex.getRequestMethod().equals("GET") && !ex.getRequestMethod().equals("HEAD")) { send(ex, 405, "text/plain", new byte[0]); return; }
        if (path.equals("/")) path = "/index.html";
        String decoded = URI.create(path).getPath();
        Path f = web.resolve(decoded.substring(1)).normalize();
        if (!f.startsWith(web) || !Files.isRegularFile(f) || decoded.contains("..")) { send(ex, 404, "text/plain; charset=utf-8", "Introuvable".getBytes(StandardCharsets.UTF_8)); return; }
        String name = f.getFileName().toString();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        send(ex, 200, MIME.getOrDefault(ext, "application/octet-stream"), Files.readAllBytes(f));
    }

    private boolean authorized(HttpExchange ex) {
        String given = ex.getRequestHeaders().getFirst("X-JQ-Token");
        if (given == null || !MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) return false;
        String origin = ex.getRequestHeaders().getFirst("Origin");
        return origin == null || origin.equals("http://127.0.0.1:" + port) || origin.equals("http://localhost:" + port);
    }

    private void api(HttpExchange ex, String path) throws IOException {
        if (!authorized(ex)) { json(ex, 403, Map.of("error", "Jeton de session invalide. Relance JavaQuest.exe.")); return; }
        String method = ex.getRequestMethod();
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        switch (method + " " + path) {
            case "GET /api/health" -> json(ex, 200, Map.of("ok", true, "version", VERSION,
                    "sandbox", runner.grantError() == null, "sandboxError", Objects.toString(runner.grantError(), "")));
            case "GET /api/content" -> json(ex, 200, content.publicView());
            case "GET /api/profiles" -> json(ex, 200, Map.of("profiles", storage.list()));
            case "POST /api/profiles" -> {
                Map<String, Object> b = Json.parseObject(body(ex, 64 * 1024));
                String id = storage.newId(Json.str(b, "name", "joueur"));
                json(ex, 200, Map.of("id", id));
            }
            case "POST /api/profiles/delete" -> {
                storage.delete(Json.str(Json.parseObject(body(ex, 4096)), "id", ""));
                json(ex, 200, Map.of("ok", true));
            }
            case "GET /api/save" -> {
                String s = storage.load(q.get("profile"));
                if (s == null) json(ex, 404, Map.of("error", "Aucune sauvegarde"));
                else send(ex, 200, "application/json; charset=utf-8", s.getBytes(StandardCharsets.UTF_8));
            }
            case "PUT /api/save" -> {
                storage.save(q.get("profile"), body(ex, (int) Storage.MAX_SAVE_BYTES));
                json(ex, 200, Map.of("ok", true));
            }
            case "POST /api/run" -> json(ex, 200, run(Json.parseObject(body(ex, 1024 * 1024)), false));
            case "POST /api/check" -> json(ex, 200, run(Json.parseObject(body(ex, 1024 * 1024)), true));
            default -> json(ex, 404, Map.of("error", "API inconnue"));
        }
    }

    /** Exécution (run) ou vérification (check) d'un code de joueur, avec l'API et le harnais de la quête. */
    private Map<String, Object> run(Map<String, Object> b, boolean check) {
        Map<String, Object> req = new LinkedHashMap<>();
        List<Object> files = new ArrayList<>();
        int total = 0;
        for (Object o : Json.list(b, "files")) {
            if (!(o instanceof Map<?, ?> f)) continue;
            String name = String.valueOf(f.get("name"));
            String code = String.valueOf(f.get("content"));
            if (!name.matches("[A-Za-z_][A-Za-z0-9_]{0,60}\\.java")) return Map.of("status", "error", "error", "Nom de fichier invalide : " + name);
            total += code.length();
            files.add(Map.of("name", name, "content", code));
        }
        if (files.isEmpty() || files.size() > 20 || total > 200_000) return Map.of("status", "error", "error", "Fichiers absents ou trop volumineux.");
        req.put("files", files);
        Content.Quest quest = content.quest(Json.str(b, "quest", ""));
        if (quest != null) req.put("support", quest.support());
        if (check) {
            if (quest == null || quest.harness().isEmpty()) return Map.of("status", "error", "error", "Cette quête n'a pas de vérification.");
            req.put("mode", "test");
            req.put("harness", quest.harness());
            req.put("entry", Json.str(quest.meta(), "entry", "Verif"));
        } else {
            req.put("mode", "run");
            req.put("stdin", Json.str(b, "stdin", ""));
            req.put("args", Json.list(b, "args"));
        }
        Map<String, Object> r = runner.run(req, check ? SandboxRunner.Limits.TEST : SandboxRunner.Limits.RUN);
        if (check) {
            List<Object> tests = r.get("tests") instanceof List<?> l ? new ArrayList<>(l) : List.of();
            boolean all = !tests.isEmpty() && tests.stream().allMatch(t -> t instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("ok")));
            r.put("passed", all && "ok".equals(r.get("status")));
        }
        return r;
    }

    private static String body(HttpExchange ex, int max) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] b = in.readNBytes(max + 1);
            if (b.length > max) throw new IllegalArgumentException("requête trop volumineuse");
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null) return m;
        for (String kv : raw.split("&")) {
            int e = kv.indexOf('=');
            if (e > 0) m.put(java.net.URLDecoder.decode(kv.substring(0, e), StandardCharsets.UTF_8), java.net.URLDecoder.decode(kv.substring(e + 1), StandardCharsets.UTF_8));
        }
        return m;
    }

    private static void json(HttpExchange ex, int status, Object o) throws IOException {
        send(ex, status, "application/json; charset=utf-8", Json.write(o).getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange ex, int status, String type, byte[] body) throws IOException {
        var h = ex.getResponseHeaders();
        h.set("Content-Type", type);
        h.set("X-Content-Type-Options", "nosniff");
        h.set("Referrer-Policy", "no-referrer");
        h.set("Cross-Origin-Resource-Policy", "same-origin");
        h.set("Cache-Control", "no-store");
        if (type.startsWith("text/html")) { h.set("Content-Security-Policy", CSP); h.set("X-Frame-Options", "DENY"); }
        boolean head = ex.getRequestMethod().equals("HEAD");
        ex.sendResponseHeaders(status, head || body.length == 0 ? -1 : body.length);
        if (!head && body.length > 0) try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
}
