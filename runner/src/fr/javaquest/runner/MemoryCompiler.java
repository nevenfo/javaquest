package fr.javaquest.runner;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Compilation en mémoire avec le vrai compilateur javac du JDK (javax.tools). */
public final class MemoryCompiler {

    public record Source(String name, String content) { }

    public record Diagnostic(String file, long line, long column, String kind, String code, String message) {
        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("file", file); m.put("line", line); m.put("column", column);
            m.put("kind", kind); m.put("code", code); m.put("message", message);
            return m;
        }
    }

    public record Result(boolean ok, List<Diagnostic> diagnostics, Map<String, byte[]> classes, Map<String, String> classToFile) { }

    private static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();

    private static final class Src extends SimpleJavaFileObject {
        final String code; final String name;
        Src(String name, String code) {
            super(URI.create("mem:///" + name.replace(' ', '_')), Kind.SOURCE);
            this.name = name; this.code = code;
        }
        @Override public CharSequence getCharContent(boolean ignore) { return code; }
        @Override public String getName() { return name; }
    }

    private static final class Out extends SimpleJavaFileObject {
        final String className; final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Out(String className) {
            super(URI.create("mem:///" + className.replace('.', '/') + ".class"), Kind.CLASS);
            this.className = className;
        }
        @Override public OutputStream openOutputStream() { return bytes; }
    }

    private static final class Fm extends ForwardingJavaFileManager<StandardJavaFileManager> {
        final Map<String, Out> outputs = new LinkedHashMap<>();
        final Map<String, String> origin = new HashMap<>();
        final Map<String, byte[]> previous;
        Fm(StandardJavaFileManager m, Map<String, byte[]> previous) { super(m); this.previous = previous; }

        @Override
        public JavaFileObject getJavaFileForOutput(Location loc, String className, JavaFileObject.Kind kind, FileObject sibling) {
            Out o = new Out(className);
            outputs.put(className, o);
            if (sibling instanceof Src s) origin.put(className, s.name);
            return o;
        }

        /** Rend visibles à javac les classes déjà compilées en mémoire (phase « harnais »). */
        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws java.io.IOException {
            Iterable<JavaFileObject> base = super.list(location, packageName, kinds, recurse);
            if (location != StandardLocation.CLASS_PATH || !kinds.contains(JavaFileObject.Kind.CLASS) || previous.isEmpty()) return base;
            List<JavaFileObject> all = new ArrayList<>();
            base.forEach(all::add);
            for (Map.Entry<String, byte[]> e : previous.entrySet()) {
                String cn = e.getKey();
                int dot = cn.lastIndexOf('.');
                String pkg = dot < 0 ? "" : cn.substring(0, dot);
                if (pkg.equals(packageName) || (recurse && pkg.startsWith(packageName))) all.add(new Prev(cn, e.getValue()));
            }
            return all;
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof Prev p) return p.className;
            return super.inferBinaryName(location, file);
        }
    }

    private static final class Prev extends SimpleJavaFileObject {
        final String className; final byte[] bytes;
        Prev(String className, byte[] bytes) {
            super(URI.create("mem:///prev/" + className.replace('.', '/') + ".class"), Kind.CLASS);
            this.className = className; this.bytes = bytes;
        }
        @Override public java.io.InputStream openInputStream() { return new java.io.ByteArrayInputStream(bytes); }
    }

    /** Compile {@code sources} ; {@code previous} = classes déjà compilées visibles (peut être vide). */
    public static Result compile(List<Source> sources, Map<String, byte[]> previous) {
        if (JAVAC == null) throw new IllegalStateException("javac indisponible dans ce runtime");
        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
        StandardJavaFileManager std = JAVAC.getStandardFileManager(diags, Locale.FRENCH, StandardCharsets.UTF_8);
        Fm fm = new Fm(std, previous);
        List<Src> units = new ArrayList<>();
        for (Source s : sources) units.add(new Src(s.name(), s.content()));
        List<String> options = List.of("-proc:none", "-g", "-encoding", "UTF-8", "-Xlint:-options", "-Xmaxerrs", "50",
                "-classpath", System.getProperty("java.class.path"));
        Boolean ok;
        try {
            ok = JAVAC.getTask(null, fm, diags, options, null, units).call();
        } catch (RuntimeException e) {
            ok = false;
        }
        List<Diagnostic> out = new ArrayList<>();
        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
            String file = d.getSource() instanceof Src s ? s.name : "";
            out.add(new Diagnostic(file, d.getLineNumber(), d.getColumnNumber(), d.getKind().name(),
                    String.valueOf(d.getCode()), d.getMessage(Locale.FRENCH)));
        }
        Map<String, byte[]> classes = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(ok)) for (Out o : fm.outputs.values()) classes.put(o.className, o.bytes.toByteArray());
        return new Result(Boolean.TRUE.equals(ok), out, classes, fm.origin);
    }

    /** Chargeur des classes compilées en mémoire. */
    public static final class Loader extends ClassLoader {
        private final Map<String, byte[]> classes;
        public Loader(Map<String, byte[]> classes, ClassLoader parent) { super("javaquest-player", parent); this.classes = classes; }
        @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] b = classes.get(name);
            if (b == null) throw new ClassNotFoundException(name);
            return defineClass(name, b, 0, b.length);
        }
    }
}
