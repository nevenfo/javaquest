package fr.javaquest.server;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * Point d'entrée de JavaQuest.cmd (lancé par le javaw.exe signé du runtime embarqué).
 * Instance unique par dossier d'installation (verrou de fichier), ouverture du navigateur,
 * journal %LOCALAPPDATA%\JavaQuest\javaquest.log, arrêt par /api/quit ou par inactivité.
 * Options : --app &lt;dir&gt;, --data &lt;dir&gt;, --no-browser, --idle-seconds &lt;n&gt; (défaut 240 ; 0 = jamais).
 */
public final class Launch {
    static final long DEFAULT_IDLE_SECONDS = 240;
    private static FileChannel held; // garde le verrou d'instance unique jusqu'à la fin du processus

    public static void main(String[] args) throws Exception {
        boolean noBrowser = false;
        Map<String, String> o = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--no-browser")) noBrowser = true;
            else if (args[i].startsWith("--") && i + 1 < args.length) o.put(args[i].substring(2), args[++i]);
        }
        Path app = o.containsKey("app") ? Path.of(o.get("app")).toAbsolutePath().normalize()
                : Path.of(Launch.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().getParent();
        String local = System.getenv("LOCALAPPDATA");
        Path data = o.containsKey("data") ? Path.of(o.get("data")).toAbsolutePath()
                : (local != null ? Path.of(local) : Path.of(System.getProperty("user.home"))).resolve("JavaQuest");
        Files.createDirectories(data);
        Path log = data.resolve("javaquest.log");
        PrintStream ps = new PrintStream(new FileOutputStream(log.toFile(), true), true, StandardCharsets.UTF_8);
        System.setOut(ps);
        System.setErr(ps);
        System.out.println("---- " + LocalDateTime.now().withNano(0) + " démarrage " + app);

        String key = key(app);
        Path session = data.resolve("session-" + key + ".txt");
        try {
            run(app, data, o, noBrowser, key, session);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(Path app, Path data, Map<String, String> o, boolean noBrowser, String key, Path session) throws Exception {
        if (!Files.isRegularFile(app.resolve("server.jar")) || !Files.isRegularFile(app.resolve("runtime/bin/javaw.exe"))
                || !Files.isDirectory(app.resolve("web")) || !Files.isRegularFile(app.resolve("bin/sandbox.jar"))) {
            System.out.println("ERREUR : fichiers du jeu introuvables dans « " + app + " ». Extrais tout le contenu du ZIP puis relance JavaQuest.cmd.");
            System.exit(1);
        }
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            try {
                if (Double.parseDouble(System.getProperty("os.version", "10").split("\\.")[0]) < 10) {
                    System.out.println("ERREUR : JavaQuest nécessite Windows 10 ou 11 (isolation AppContainer).");
                    System.exit(1);
                }
            } catch (NumberFormatException ignored) { }
        }
        FileChannel ch = FileChannel.open(data.resolve("lock-" + key + ".lck"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        held = ch;
        FileLock lock = null;
        long deadline = System.nanoTime() + 60_000_000_000L;
        while (lock == null) {
            lock = ch.tryLock();
            if (lock != null) break;
            // Une instance tourne déjà : on rouvre le jeu (la session est publiée une fois le serveur prêt).
            String url = Files.isRegularFile(session) ? Files.readString(session, StandardCharsets.UTF_8).trim() : "";
            if (!url.isEmpty()) {
                System.out.println("Instance déjà active : " + (noBrowser ? "navigateur non ouvert." : "ouverture du navigateur."));
                if (!noBrowser) openBrowser(url);
                return;
            }
            if (System.nanoTime() > deadline) {
                System.out.println("ERREUR : l'instance en cours ne répond pas (pas de session publiée).");
                System.exit(1);
            }
            Thread.sleep(200);
        }
        // Verrou obtenu : toute session présente est périmée (arrêt brutal précédent).
        Files.deleteIfExists(session);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { Files.deleteIfExists(session); } catch (Exception ignored) { }
        }));
        Map<String, String> so = new HashMap<>(o);
        so.put("app", app.toString());
        so.put("data", data.toString());
        so.putIfAbsent("idle-seconds", String.valueOf(DEFAULT_IDLE_SECONDS));
        Main m = Main.start(so);
        Path tmp = data.resolve("session-" + key + ".tmp");
        Files.writeString(tmp, m.url(), StandardCharsets.UTF_8);
        Files.move(tmp, session, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        System.out.println("Serveur prêt (le jeton n'est pas journalisé).");
        if (!noBrowser) openBrowser(m.url());
        // Le thread principal se termine ; le serveur (threads non daemon) continue jusqu'à /api/quit ou l'inactivité.
    }

    private static void openBrowser(String url) {
        // L'URL est générée localement (http://127.0.0.1:<port>/#t=<hex>) ; pas de shell, argument séparé.
        if (!url.matches("http://127\\.0\\.0\\.1:\\d+/#t=[0-9a-f]+")) { System.out.println("URL inattendue, navigateur non ouvert."); return; }
        String root = System.getenv("SystemRoot");
        String rundll = (root != null ? root : "C:\\Windows") + "\\System32\\rundll32.exe";
        try {
            new ProcessBuilder(rundll, "url.dll,FileProtocolHandler", url).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        } catch (Exception e) {
            System.out.println("Impossible d'ouvrir le navigateur : " + e.getMessage() + " — ouvre manuellement le jeu (voir session dans le dossier de données).");
        }
    }

    /** Même clé que l'ancien lanceur : 6 premiers octets de SHA-256(chemin de app en minuscules), en hexadécimal. */
    static String key(Path app) throws Exception {
        byte[] h = MessageDigest.getInstance("SHA-256").digest(app.toString().toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(h, 0, 6);
    }
}
