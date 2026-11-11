package fr.javaquest.runner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Outils des harnais de vérification (fichiers cachés des quêtes). Chaque {@link #test} produit un résultat T.
 */
public final class Check {
    private Check() { }

    /** Échec d'une vérification, avec message pédagogique. */
    public static final class Failure extends RuntimeException {
        public Failure(String message) { super(message, null, false, false); }
    }

    @FunctionalInterface
    public interface Body { void run() throws Throwable; }

    private static int passed, failed;

    public static void test(String name, Body body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        try {
            body.run();
            m.put("ok", true);
            passed++;
        } catch (Failure f) {
            m.put("ok", false);
            m.put("message", f.getMessage());
            failed++;
        } catch (Throwable t) {
            if (t instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null) t = ite.getCause();
            m.put("ok", false);
            m.put("message", "Ton code a levé " + t.getClass().getSimpleName() + (t.getMessage() != null ? " : " + t.getMessage() : ""));
            m.put("exception", Runner.describe(t));
            failed++;
        }
        Trace.send("T", m);
    }

    public static void fail(String message) { throw new Failure(message); }

    public static void isTrue(boolean condition, String message) { if (!condition) throw new Failure(message); }

    public static void equal(Object expected, Object actual, String what) {
        if (!Objects.equals(expected, actual))
            throw new Failure(what + " : attendu " + Values.describe(expected) + ", obtenu " + Values.describe(actual));
    }

    public static void near(double expected, double actual, double eps, String what) {
        if (!(Math.abs(expected - actual) <= eps))
            throw new Failure(what + " : attendu ≈ " + expected + ", obtenu " + actual);
    }

    /** Sortie standard capturée d'un main du joueur (stdin fourni). */
    public static String runMain(String className, String stdin, String... args) throws Throwable {
        Class<?> c = playerClass(className);
        InputStream oldIn = System.in;
        StringBuilder sb = new StringBuilder();
        System.out.flush();
        synchronized (Capture.class) { Capture.redirect = sb; }
        System.setIn(new ByteArrayInputStream((stdin == null ? "" : stdin).getBytes(StandardCharsets.UTF_8)));
        try {
            Runner.invokeMain(c, args);
        } finally {
            System.out.flush();
            System.err.flush();
            synchronized (Capture.class) { Capture.redirect = null; }
            System.setIn(oldIn);
        }
        return sb.toString().replace("\r\n", "\n");
    }

    /** Classe du joueur par nom, ou échec explicite si elle n'existe pas. */
    public static Class<?> playerClass(String name) {
        try {
            return Class.forName(name, true, Runner.loader());
        } catch (ClassNotFoundException e) {
            throw new Failure("La classe " + name + " est introuvable dans ton code.");
        }
    }

    /** Lignes non vides de la sortie, sans espaces de fin. */
    public static List<String> lines(String out) {
        List<String> l = new ArrayList<>();
        for (String s : out.split("\n")) { String t = s.stripTrailing(); if (!t.isEmpty()) l.add(t); }
        return l;
    }

    static Map<String, Object> summary() { return Map.of("passed", passed, "failed", failed); }
}
