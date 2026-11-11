package fr.javaquest.runner;

import java.util.Arrays;

/** Représentation textuelle sûre (bornée, sans exception) d'une valeur Java, façon débogueur. */
public final class Values {
    private Values() { }

    public static String describe(Object v) {
        String s;
        try {
            if (v == null) s = "null";
            else if (v instanceof String str) s = '"' + str + '"';
            else if (v instanceof Character c) s = "'" + c + "'";
            else if (v instanceof Object[] a) s = Arrays.deepToString(a);
            else if (v instanceof int[] a) s = Arrays.toString(a);
            else if (v instanceof long[] a) s = Arrays.toString(a);
            else if (v instanceof double[] a) s = Arrays.toString(a);
            else if (v instanceof float[] a) s = Arrays.toString(a);
            else if (v instanceof boolean[] a) s = Arrays.toString(a);
            else if (v instanceof char[] a) s = Arrays.toString(a);
            else if (v instanceof byte[] a) s = Arrays.toString(a);
            else if (v instanceof short[] a) s = Arrays.toString(a);
            else s = String.valueOf(v);
        } catch (Throwable t) {
            s = "<toString() a levé " + t.getClass().getSimpleName() + ">";
        }
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }
}
