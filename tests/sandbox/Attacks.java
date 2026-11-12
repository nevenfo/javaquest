import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * Programme hostile de test : chaque mode tente une action interdite.
 * Sortie : "ALLOWED <détail>" si l'action a réussi, "BLOCKED <exception>" sinon.
 * Exécuté uniquement par tools/test-sandbox.sh à l'intérieur du broker d'isolation (sandbox.jar).
 */
public class Attacks {
    public static void main(String[] args) throws Exception {
        String mode = args[0];
        String arg = args.length > 1 ? args[1] : "";
        try {
            switch (mode) {
                case "ok" -> System.out.println("ALLOWED hello");
                case "read" -> System.out.println("ALLOWED " + Files.readString(Path.of(arg)).length() + " chars");
                case "list" -> System.out.println("ALLOWED " + Arrays.toString(new File(arg).list()));
                case "write" -> { Files.writeString(Path.of(arg), "pwn"); System.out.println("ALLOWED wrote " + arg); }
                case "writeTemp" -> { Path p = Files.createTempFile("jq", ".txt"); System.out.println("ALLOWED " + p); }
                case "net" -> {
                    String[] hp = arg.split(":");
                    try (Socket s = new Socket()) { s.connect(new InetSocketAddress(hp[0], Integer.parseInt(hp[1])), 3000); }
                    System.out.println("ALLOWED connected " + arg);
                }
                case "dns" -> System.out.println("ALLOWED " + InetAddress.getByName(arg));
                case "listen" -> { try (ServerSocket ss = new ServerSocket(0)) { System.out.println("ALLOWED listening " + ss.getLocalPort()); } }
                case "proc" -> {
                    Process p = new ProcessBuilder("cmd.exe", "/c", "echo pwn").start();
                    System.out.println("ALLOWED process " + p.pid() + " exit " + p.waitFor());
                }
                case "prefs" -> {
                    var prefs = java.util.prefs.Preferences.userRoot().node("javaquest-attack");
                    prefs.put("k", "v"); prefs.flush();
                    System.out.println("ALLOWED prefs write");
                }
                case "env" -> System.out.println("ALLOWED " + System.getenv().keySet());
                case "loop" -> { while (true) { } }
                case "sleep" -> { Thread.sleep(600_000); }
                case "mem" -> {
                    List<long[]> l = new ArrayList<>();
                    while (true) l.add(new long[1 << 20]);
                }
                case "threads" -> {
                    int n = 0;
                    try { while (n < 100_000) { Thread t = new Thread(() -> { try { Thread.sleep(600_000); } catch (InterruptedException e) { } }); t.start(); n++; } }
                    finally { System.out.println("threads started " + n); }
                }
                case "flood" -> { String s = "x".repeat(1000); while (true) System.out.println(s); }
                case "persist" -> {
                    Thread t = new Thread(() -> { while (true) { } });
                    t.setDaemon(false); t.start();
                    System.out.println("main returned");
                }
                default -> System.out.println("unknown mode");
            }
        } catch (Throwable t) {
            System.out.println("BLOCKED " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }
}
