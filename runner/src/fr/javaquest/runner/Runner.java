package fr.javaquest.runner;

import fr.javaquest.common.Json;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Point d'entrée exécuté DANS la sandbox. Lit une requête JSON sur stdin :
 * <pre>{nonce, mode:"run"|"test", files:[{name,content}], support:[...], harness:[...],
 *   main?, entry?, stdin?, args?}</pre>
 * Émet des lignes de protocole (voir {@link Trace}) : C (compilation), O (sortie), E (action),
 * W (valeur observée), X (exception), T (test), L (limite), D (fin).
 */
public final class Runner {
    private static MemoryCompiler.Loader loader;
    private static volatile boolean done;
    private static long start;

    private Runner() { }

    static ClassLoader loader() { return loader; }

    public static void main(String[] argv) throws Exception {
        PrintStream real = new PrintStream(new FileOutputStream(FileDescriptor.out), false, StandardCharsets.UTF_8);
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> req = Json.parseObject(input);
        Trace.init(real, Json.str(req, "nonce", "nonce"));
        start = System.nanoTime();

        List<MemoryCompiler.Source> player = sources(Json.list(req, "files"));
        List<MemoryCompiler.Source> support = sources(Json.list(req, "support"));
        List<MemoryCompiler.Source> all = new ArrayList<>(player);
        all.addAll(support);

        MemoryCompiler.Result r = MemoryCompiler.compile(all, Map.of());
        Trace.send("C", compileReport("player", r));
        if (!r.ok()) { finish("compile-error"); return; }

        Set<String> playerFiles = new HashSet<>();
        player.forEach(s -> playerFiles.add(s.name()));
        List<String> playerClasses = new ArrayList<>();
        r.classToFile().forEach((cls, file) -> { if (playerFiles.contains(file)) playerClasses.add(cls); });
        Trace.playerClasses(playerClasses);

        Map<String, byte[]> classes = new LinkedHashMap<>(r.classes());
        boolean test = "test".equals(Json.str(req, "mode", "run"));
        if (test) {
            MemoryCompiler.Result h = MemoryCompiler.compile(sources(Json.list(req, "harness")), r.classes());
            Trace.send("C", compileReport("harness", h));
            if (!h.ok()) { finish("structure-error"); return; }
            classes.putAll(h.classes());
        }
        loader = new MemoryCompiler.Loader(classes, Runner.class.getClassLoader());

        System.setIn(new ByteArrayInputStream(Json.str(req, "stdin", "").getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(new Capture("out"), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new Capture("err"), true, StandardCharsets.UTF_8));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!done) { System.out.flush(); System.err.flush(); finish("exit"); }
        }));

        String status = "ok";
        try {
            if (test) {
                Class<?> entry = Class.forName(Json.str(req, "entry", "Verif"), true, loader);
                Method v = entry.getDeclaredMethod("verifier");
                v.setAccessible(true);
                v.invoke(null);
            } else {
                Class<?> main = mainClass(Json.str(req, "main", null), playerClasses, player);
                List<String> args = new ArrayList<>();
                for (Object o : Json.list(req, "args")) args.add(String.valueOf(o));
                invokeMain(main, args.toArray(new String[0]));
            }
        } catch (Throwable t) {
            Throwable cause = t instanceof InvocationTargetException ite && ite.getCause() != null ? ite.getCause() : t;
            System.out.flush(); System.err.flush();
            Trace.send("X", describe(cause));
            status = "exception";
        }
        System.out.flush();
        System.err.flush();
        finish(status);
        Runtime.getRuntime().halt(0);
    }

    private static synchronized void finish(String status) {
        if (done) return;
        done = true;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        m.put("ms", (System.nanoTime() - start) / 1_000_000);
        m.put("tests", Check.summary());
        Trace.send("D", m);
    }

    private static List<MemoryCompiler.Source> sources(List<Object> list) {
        List<MemoryCompiler.Source> out = new ArrayList<>();
        for (Object o : list) {
            @SuppressWarnings("unchecked") Map<String, Object> f = (Map<String, Object>) o;
            out.add(new MemoryCompiler.Source(Json.str(f, "name", "Main.java"), Json.str(f, "content", "")));
        }
        return out;
    }

    private static Map<String, Object> compileReport(String phase, MemoryCompiler.Result r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("phase", phase);
        m.put("ok", r.ok());
        List<Object> d = new ArrayList<>();
        r.diagnostics().forEach(x -> d.add(x.toMap()));
        m.put("diagnostics", d);
        return m;
    }

    /** Choisit la classe principale : celle demandée, sinon celle du premier fichier qui possède un main. */
    private static Class<?> mainClass(String requested, List<String> playerClasses, List<MemoryCompiler.Source> player) throws Exception {
        if (requested != null) return Class.forName(requested, true, loader);
        List<String> ordered = new ArrayList<>();
        for (MemoryCompiler.Source s : player) {
            String base = s.name().replaceFirst("\\.java$", "");
            for (String c : playerClasses) if (c.equals(base) || c.endsWith("." + base)) ordered.add(c);
        }
        for (String c : playerClasses) if (!ordered.contains(c)) ordered.add(c);
        for (String c : ordered) {
            Class<?> k = Class.forName(c, false, loader);
            if (findMain(k) != null) return Class.forName(c, true, loader);
        }
        throw new NoSuchMethodException("Aucune méthode main trouvée : écris void main() ou public static void main(String[] args).");
    }

    /** Ordre de recherche du lanceur Java 25 (JEP 512) : main(String[]) puis main(), statique ou d'instance, non privée. */
    static Method findMain(Class<?> k) {
        for (boolean withArgs : new boolean[]{true, false}) {
            for (Class<?> c = k; c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    Method m = withArgs ? c.getDeclaredMethod("main", String[].class) : c.getDeclaredMethod("main");
                    if (!Modifier.isPrivate(m.getModifiers()) && m.getReturnType() == void.class) return m;
                } catch (NoSuchMethodException ignored) { }
            }
        }
        return null;
    }

    static void invokeMain(Class<?> k, String[] args) throws Throwable {
        Method m = findMain(k);
        if (m == null) throw new NoSuchMethodException("Aucune méthode main dans " + k.getSimpleName());
        m.setAccessible(true);
        Object target = null;
        if (!Modifier.isStatic(m.getModifiers())) {
            var ctor = k.getDeclaredConstructor();
            ctor.setAccessible(true);
            target = ctor.newInstance();
        }
        try {
            if (m.getParameterCount() == 1) m.invoke(target, (Object) args); else m.invoke(target);
        } catch (InvocationTargetException e) {
            throw e.getCause() != null ? e.getCause() : e;
        }
    }

    /** Description pédagogique d'une exception : type, message, frames du joueur. */
    static Map<String, Object> describe(Throwable t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", t.getClass().getName());
        m.put("simpleType", t.getClass().getSimpleName());
        m.put("message", t.getMessage());
        List<Object> frames = new ArrayList<>();
        for (StackTraceElement e : t.getStackTrace()) {
            String cn = e.getClassName();
            boolean mine = Trace.isPlayerClass(cn);
            if (cn.startsWith("fr.javaquest.runner.") || cn.startsWith("jdk.internal.reflect") || cn.startsWith("java.lang.reflect")) continue;
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("class", cn); f.put("method", e.getMethodName()); f.put("file", e.getFileName()); f.put("line", e.getLineNumber());
            f.put("player", mine);
            frames.add(f);
            if (frames.size() >= 12) break;
        }
        m.put("frames", frames);
        if (t.getCause() != null && t.getCause() != t) m.put("cause", t.getCause().getClass().getSimpleName() + ": " + t.getCause().getMessage());
        return m;
    }
}
