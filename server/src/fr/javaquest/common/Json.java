package fr.javaquest.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON minimal sans dépendance : objets → {@code Map<String,Object>} (ordre conservé),
 * tableaux → {@code List<Object>}, nombres → {@code Long} ou {@code Double}, plus String, Boolean, null.
 */
public final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        Json p = new Json(text);
        p.ws();
        Object v = p.value(0);
        p.ws();
        if (p.i != p.s.length()) throw p.error("caractères en trop");
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object o = parse(text);
        if (!(o instanceof Map)) throw new IllegalArgumentException("objet JSON attendu");
        return (Map<String, Object>) o;
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException("JSON invalide (" + msg + ") à la position " + i);
    }

    private void ws() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++; else break;
        }
    }

    private Object value(int depth) {
        if (depth > 64) throw error("imbrication trop profonde");
        if (i >= s.length()) throw error("fin inattendue");
        char c = s.charAt(i);
        switch (c) {
            case '{': return object(depth);
            case '[': return array(depth);
            case '"': return string();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) return number();
                throw error("caractère inattendu '" + c + "'");
        }
    }

    private void expect(String word) {
        if (!s.startsWith(word, i)) throw error(word + " attendu");
        i += word.length();
    }

    private Map<String, Object> object(int depth) {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;
        ws();
        if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            if (i >= s.length() || s.charAt(i) != '"') throw error("clé attendue");
            String k = string();
            ws();
            if (i >= s.length() || s.charAt(i) != ':') throw error("':' attendu");
            i++;
            ws();
            m.put(k, value(depth + 1));
            ws();
            if (i >= s.length()) throw error("fin inattendue");
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw error("',' ou '}' attendu");
        }
    }

    private List<Object> array(int depth) {
        List<Object> l = new ArrayList<>();
        i++;
        ws();
        if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
        while (true) {
            ws();
            l.add(value(depth + 1));
            ws();
            if (i >= s.length()) throw error("fin inattendue");
            char c = s.charAt(i++);
            if (c == ']') return l;
            if (c != ',') throw error("',' ou ']' attendu");
        }
    }

    private String string() {
        StringBuilder b = new StringBuilder();
        i++;
        while (true) {
            if (i >= s.length()) throw error("chaîne non terminée");
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c != '\\') { b.append(c); continue; }
            if (i >= s.length()) throw error("échappement incomplet");
            char e = s.charAt(i++);
            switch (e) {
                case '"': b.append('"'); break;
                case '\\': b.append('\\'); break;
                case '/': b.append('/'); break;
                case 'b': b.append('\b'); break;
                case 'f': b.append('\f'); break;
                case 'n': b.append('\n'); break;
                case 'r': b.append('\r'); break;
                case 't': b.append('\t'); break;
                case 'u':
                    if (i + 4 > s.length()) throw error("\\u incomplet");
                    b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                    break;
                default: throw error("échappement inconnu");
            }
        }
    }

    private Object number() {
        int start = i;
        if (s.charAt(i) == '-') i++;
        while (i < s.length() && "0123456789.eE+-".indexOf(s.charAt(i)) >= 0) i++;
        String t = s.substring(start, i);
        try {
            if (t.contains(".") || t.contains("e") || t.contains("E")) return Double.parseDouble(t);
            return Long.parseLong(t);
        } catch (NumberFormatException ex) {
            throw error("nombre invalide");
        }
    }

    // ---------- Écriture ----------

    public static String write(Object o) {
        StringBuilder b = new StringBuilder();
        write(b, o);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    public static void write(StringBuilder b, Object o) {
        if (o == null) b.append("null");
        else if (o instanceof String str) quote(b, str);
        else if (o instanceof Boolean || o instanceof Integer || o instanceof Long || o instanceof Short || o instanceof Byte) b.append(o);
        else if (o instanceof Double d) b.append(d.isNaN() || d.isInfinite() ? "null" : (d == Math.rint(d) && Math.abs(d) < 1e15 ? String.valueOf(d.longValue()) : d.toString()));
        else if (o instanceof Float f) write(b, f.doubleValue());
        else if (o instanceof Map<?, ?> m) {
            b.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) b.append(',');
                first = false;
                quote(b, String.valueOf(e.getKey()));
                b.append(':');
                write(b, e.getValue());
            }
            b.append('}');
        } else if (o instanceof Iterable<?> it) {
            b.append('[');
            boolean first = true;
            for (Object x : it) {
                if (!first) b.append(',');
                first = false;
                write(b, x);
            }
            b.append(']');
        } else if (o instanceof Object[] arr) write(b, List.of(arr));
        else quote(b, o.toString());
    }

    public static void quote(StringBuilder b, String s) {
        b.append('"');
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20 || c == 0x2028 || c == 0x2029) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        b.append('"');
    }

    // ---------- Accès typés ----------

    public static String str(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return v instanceof String s ? s : def;
    }

    public static long num(Map<String, Object> m, String k, long def) {
        Object v = m.get(k);
        return v instanceof Number n ? n.longValue() : def;
    }

    public static boolean bool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        return v instanceof Boolean b ? b : def;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> list(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v instanceof List ? (List<Object>) v : List.of();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v instanceof Map ? (Map<String, Object>) v : Map.of();
    }
}
