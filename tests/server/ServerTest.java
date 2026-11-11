import fr.javaquest.common.Json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Tests d'intégration : démarre le vrai serveur (sous-processus) puis vérifie sécurité HTTP,
 * exécution isolée (correct, erreur de compilation, exception, boucle infinie, sondes, entrée),
 * vérification de quête et sauvegardes. Usage : java -cp build/server/server.jar tests/server/ServerTest.java <args serveur...>
 */
public class ServerTest {
    static int pass, fail;
    static String base, token;
    static final HttpClient http = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "fr.javaquest.server.Main", "--watch-stdin", "1"));
        cmd.addAll(List.of(args));
        Process server = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        BufferedReader r = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
        String ready = r.readLine();
        if (ready == null || !ready.startsWith("JAVAQUEST_READY ")) { System.out.println("FAIL démarrage : " + ready); System.exit(1); }
        String url = ready.substring("JAVAQUEST_READY ".length());
        base = url.substring(0, url.indexOf("/#t="));
        token = url.substring(url.indexOf("#t=") + 3);
        try {
            run();
        } finally {
            server.getOutputStream().close(); // fermeture de stdin → arrêt du serveur
            boolean stopped = server.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            check("arrêt propre à la fermeture de stdin", stopped);
            if (!stopped) server.destroyForcibly();
        }
        System.out.println("RÉSULTAT : " + pass + " PASS, " + fail + " FAIL");
        System.exit(fail == 0 ? 0 : 1);
    }

    static void check(String name, boolean ok) { check(name, ok, ""); }

    static void check(String name, boolean ok, Object detail) {
        if (ok) { pass++; System.out.println("PASS  " + name); }
        else { fail++; System.out.println("FAIL  " + name + "  " + String.valueOf(detail).replace('\n', ' ').substring(0, Math.min(500, String.valueOf(detail).length()))); }
    }

    static HttpResponse<String> req(String method, String path, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path));
        headers.forEach(b::header);
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    static Map<String, Object> api(String method, String path, Object body) throws Exception {
        HttpResponse<String> res = req(method, path, body == null ? null : Json.write(body), Map.of("X-JQ-Token", token));
        return Json.parseObject(res.body());
    }

    static Map<String, Object> runCode(String code, String stdin) throws Exception {
        return api("POST", "/api/run", Map.of("files", List.of(Map.of("name", "Main.java", "content", code)), "stdin", stdin));
    }

    @SuppressWarnings("unchecked")
    static void run() throws Exception {
        check("index servi", req("GET", "/", null, Map.of()).statusCode() == 200);
        check("CSP présente", req("GET", "/", null, Map.of()).headers().firstValue("Content-Security-Policy").isPresent());
        check("traversée de chemin refusée", req("GET", "/../server.jar", null, Map.of()).statusCode() == 404);
        check("API sans jeton refusée", req("GET", "/api/health", null, Map.of()).statusCode() == 403);
        check("API mauvais jeton refusée", req("GET", "/api/health", null, Map.of("X-JQ-Token", "x")).statusCode() == 403);
        check("API origine étrangère refusée", req("POST", "/api/run", "{}", Map.of("X-JQ-Token", token, "Origin", "http://evil.example")).statusCode() == 403);
        Map<String, Object> h = api("GET", "/api/health", null);
        check("santé + sandbox prête", Boolean.TRUE.equals(h.get("sandbox")), h);

        Map<String, Object> ok = runCode("void main() {\n  String n = IO.readln();\n  IO.println(\"Salut \" + n);\n}\n", "Ada\n");
        check("exécution : stdin (écho terminal) + sortie", "ok".equals(ok.get("status")) && "Ada\nSalut Ada\n".equals(ok.get("stdout")), ok);

        Map<String, Object> ce = runCode("void main() {\n  int x = \"a\";\n}\n", "");
        List<Object> comp = (List<Object>) ce.get("compile");
        Map<String, Object> d0 = comp.isEmpty() ? Map.of() : (Map<String, Object>) ((List<Object>) ((Map<String, Object>) comp.get(0)).get("diagnostics")).get(0);
        check("erreur de compilation réelle + ligne", "compile-error".equals(ce.get("status")) && Long.valueOf(2).equals(d0.get("line"))
                && "compiler.err.prob.found.req".equals(d0.get("code")), ce);

        Map<String, Object> ex = runCode("void main() {\n  int[] t = new int[1];\n  t[3] = 0;\n}\n", "");
        Map<String, Object> exc = (Map<String, Object>) ex.getOrDefault("exception", Map.of());
        List<Object> frames = (List<Object>) exc.getOrDefault("frames", List.of());
        check("exception localisée", "exception".equals(ex.get("status")) && "ArrayIndexOutOfBoundsException".equals(exc.get("simpleType"))
                && !frames.isEmpty() && Long.valueOf(3).equals(((Map<String, Object>) frames.get(0)).get("line")), ex);

        long t0 = System.currentTimeMillis();
        Map<String, Object> loop = runCode("void main() {\n  IO.println(\"avant\");\n  while (true) { }\n}\n", "");
        long dt = System.currentTimeMillis() - t0;
        check("boucle infinie interrompue (< 15 s) avec sortie partielle", "timeout".equals(loop.get("status")) && dt < 15000
                && "avant\n".equals(loop.get("stdout")), loop.get("status") + " " + dt + "ms " + loop.get("stdout"));

        Map<String, Object> w = runCode("void main() {\n  int mana = 1;\n  for (int i = 0; i < 3; i++) {\n    mana *= 2;\n    Sonde.voir(\"mana\", mana);\n  }\n}\n", "");
        List<Object> tl = (List<Object>) w.getOrDefault("timeline", List.of());
        long watches = tl.stream().filter(e -> "watch".equals(((Map<String, Object>) e).get("t"))).count();
        check("sondes : 3 valeurs relevées ligne 5", watches == 3 && Long.valueOf(5).equals(((Map<String, Object>) tl.get(0)).get("line"))
                && "8".equals(((Map<String, Object>) tl.get(2)).get("value")), tl);

        Map<String, Object> io = runCode("import java.nio.file.*;\nvoid main() throws Exception {\n  IO.println(Files.readString(Path.of(System.getenv(\"USERPROFILE\"), \".gitconfig\")));\n}\n", "");
        check("lecture du profil depuis /api/run bloquée", "exception".equals(io.get("status")), io.get("exception"));

        Map<String, Object> mf = api("POST", "/api/run", Map.of("files", List.of(
                Map.of("name", "Main.java", "content", "public class Main {\n  public static void main(String[] a) {\n    System.out.println(new Calcul().somme(2, 3));\n  }\n}\n"),
                Map.of("name", "Calcul.java", "content", "public class Calcul {\n  int somme(int a, int b) { return a + b; }\n}\n"))));
        check("multi-fichiers", "5\n".equals(mf.get("stdout")), mf);

        Map<String, Object> good = api("POST", "/api/check", Map.of("quest", "q01-porte", "files", List.of(Map.of("name", "Main.java", "content", "void main() {\n  IO.println(\"Ouvre-toi !\");\n}\n"))));
        check("vérification : solution acceptée", Boolean.TRUE.equals(good.get("passed")), good);
        Map<String, Object> bad = api("POST", "/api/check", Map.of("quest", "q01-porte", "files", List.of(Map.of("name", "Main.java", "content", "void main() {\n  IO.println(\"Ouvre-toi\");\n}\n"))));
        check("vérification : erreur refusée avec message", Boolean.FALSE.equals(bad.get("passed")) && String.valueOf(bad.get("tests")).contains("attendu"), bad);

        Map<String, Object> content = api("GET", "/api/content", null);
        check("contenu public sans harnais ni solutions", !String.valueOf(content).contains("Check.") && !String.valueOf(content).contains("solutions/"), "fuite");

        String id = String.valueOf(api("POST", "/api/profiles", Map.of("name", "Testeur Élève")).get("id"));
        check("création de profil (id normalisé)", id.startsWith("testeur-eleve"), id);
        api("PUT", "/api/save?profile=" + id, Map.of("schema", 1, "player", Map.of("name", "Testeur Élève"), "xp", 42));
        api("PUT", "/api/save?profile=" + id, Map.of("schema", 1, "player", Map.of("name", "Testeur Élève"), "xp", 43));
        Map<String, Object> loaded = api("GET", "/api/save?profile=" + id, null);
        check("sauvegarde / chargement", Long.valueOf(43).equals(loaded.get("xp")), loaded);
        HttpResponse<String> invalid = req("PUT", "/api/save?profile=" + id, "{\"x\":1}", Map.of("X-JQ-Token", token));
        check("sauvegarde sans schéma refusée", invalid.statusCode() == 400 && invalid.body().contains("schema"), invalid.body());
        check("profil invalide refusé", req("GET", "/api/save?profile=../../etc", null, Map.of("X-JQ-Token", token)).statusCode() >= 400);
        List<Object> profiles = (List<Object>) api("GET", "/api/profiles", null).get("profiles");
        check("liste des profils", profiles.stream().anyMatch(p -> id.equals(((Map<String, Object>) p).get("id"))), profiles);
        api("POST", "/api/profiles/delete", Map.of("id", id));
        check("suppression réversible", api("GET", "/api/save?profile=" + id, null).containsKey("error"));
    }
}
