package fr.javaquest.runner;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Entrée standard du joueur, installée UNE fois comme System.in (la classe IO du JDK garde sa référence).
 * Sert au plus une ligne par lecture et la renvoie en écho sur la sortie, comme le ferait un terminal :
 * « Combien de sacs ? 23 » apparaît donc sur une seule ligne, puis la suite du programme.
 */
final class PlayerInput extends InputStream {
    private byte[] data = new byte[0];
    private int pos;
    private PrintStream echo;

    synchronized void reset(byte[] bytes, PrintStream echoTo) { data = bytes; pos = 0; echo = echoTo; }

    @Override public synchronized int read() {
        if (pos >= data.length) return -1;
        int b = data[pos++] & 0xff;
        if (echo != null) { echo.write(b); echo.flush(); }
        return b;
    }

    @Override public synchronized int read(byte[] b, int off, int len) {
        if (len == 0) return 0;
        if (pos >= data.length) return -1;
        int n = 0;
        while (n < len && pos < data.length) {
            byte c = data[pos++];
            b[off + n++] = c;
            if (c == '\n') break;
        }
        if (echo != null) { echo.write(b, off, n); echo.flush(); }
        return n;
    }

    @Override public synchronized int available() { return 0; }
}
