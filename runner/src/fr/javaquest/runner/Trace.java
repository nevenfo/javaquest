package fr.javaquest.runner;

import fr.javaquest.common.Json;

import java.io.PrintStream;
import java.util.*;

/**
 * Canal de sortie fiable vers le serveur : lignes {@code \u0001<nonce> <TYPE> <json>}.
 * Les classes du monde (API de quête) appellent {@link #event} ; la ligne source du joueur
 * est retrouvée dans la pile d'appels (première frame appartenant à une classe du joueur).
 */
public final class Trace {
    private static PrintStream channel;
    private static String prefix;
    private static final Set<String> playerClasses = new HashSet<>();
    private static int events;
    private static long outputChars;
    private static boolean truncated;
    static final int MAX_EVENTS = 4000;
    static final long MAX_OUTPUT_CHARS = 120_000;

    private Trace() { }

    static synchronized void init(PrintStream realOut, String nonce) {
        channel = realOut;
        prefix = "\u0001" + nonce + " ";
    }

    static synchronized void playerClasses(Collection<String> names) { playerClasses.addAll(names); }

    static synchronized boolean isPlayerClass(String className) {
        int d = className.indexOf('$');
        return playerClasses.contains(d < 0 ? className : className.substring(0, d));
    }

    static synchronized void send(String type, Object payload) {
        if (channel == null) return;
        StringBuilder b = new StringBuilder(prefix).append(type).append(' ');
        Json.write(b, payload);
        b.append('\n');
        channel.print(b);
        channel.flush();
    }

    /** Position (fichier, ligne) du code du joueur le plus proche dans la pile, ou null. */
    public static StackWalker.StackFrame playerFrame() {
        return StackWalker.getInstance().walk(s -> s.filter(f -> {
            String cn = f.getClassName();
            int d = cn.indexOf('$');
            return playerClasses.contains(d < 0 ? cn : cn.substring(0, d));
        }).findFirst().orElse(null));
    }

    private static Map<String, Object> located(Map<String, Object> m) {
        StackWalker.StackFrame f = playerFrame();
        if (f != null) { m.put("file", f.getFileName()); m.put("line", f.getLineNumber()); }
        return m;
    }

    private static synchronized boolean budget() {
        if (events >= MAX_EVENTS) {
            if (!truncated) { truncated = true; send("L", Map.of("reason", "events")); }
            return false;
        }
        events++;
        return true;
    }

    /** Action visible dans le monde (ex. "porte.ouvrir"). Arguments convertis en texte/nombres/booléens. */
    public static void event(String name, Object... args) {
        if (!budget()) return;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        List<Object> a = new ArrayList<>();
        for (Object o : args) a.add(o == null || o instanceof Number || o instanceof Boolean ? o : String.valueOf(o));
        m.put("args", a);
        send("E", located(m));
    }

    /** Valeur observée (sonde de débogage). */
    public static void watch(String label, Object value) {
        if (!budget()) return;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("value", Values.describe(value));
        m.put("type", value == null ? "null" : value.getClass().getSimpleName());
        send("W", located(m));
    }

    static synchronized void output(String stream, String text) {
        if (outputChars >= MAX_OUTPUT_CHARS) return;
        outputChars += text.length();
        if (outputChars >= MAX_OUTPUT_CHARS) {
            text = text.substring(0, (int) Math.max(0, text.length() - (outputChars - MAX_OUTPUT_CHARS)));
            send("L", Map.of("reason", "output"));
        }
        if (!budget()) return;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("s", stream);
        m.put("text", text);
        send("O", located(m));
    }
}
