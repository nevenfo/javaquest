package fr.javaquest.runner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Flux remplaçant System.out / System.err : chaque ligne devient un événement localisé. */
final class Capture extends OutputStream {
    private final String stream;
    private final ByteArrayOutputStream line = new ByteArrayOutputStream();
    /** Si non nul, la sortie est détournée vers ce tampon (Check.runMain). */
    static StringBuilder redirect;

    Capture(String stream) { this.stream = stream; }

    @Override public synchronized void write(int b) {
        line.write(b);
        if (b == '\n' || line.size() >= 4000) emit();
    }

    @Override public synchronized void write(byte[] b, int off, int len) {
        for (int k = off; k < off + len; k++) write(b[k]);
    }

    @Override public synchronized void flush() {
        if (line.size() > 0) emit();
    }

    private void emit() {
        String text = line.toString(StandardCharsets.UTF_8);
        line.reset();
        synchronized (Capture.class) {
            if (redirect != null) { if (redirect.length() < 200_000) redirect.append(text); return; }
        }
        Trace.output(stream, text);
    }
}
