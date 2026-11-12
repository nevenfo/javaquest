import java.util.*;

void main() {
    String ligne = " Ada : 12 ; Zoe:7;Bob :12 ";
    String[] morceaux = ligne.split(";");
    List<String> noms = new ArrayList<>();
    for (String m : morceaux) {
        String[] kv = m.split(":");
        noms.add(kv[0].trim() + "=" + kv[1].trim());
    }
    IO.println(morceaux.length);
    IO.println(noms);
    noms.sort(Comparator.comparingInt((String x) -> Integer.parseInt(x.substring(x.indexOf('=') + 1))).reversed().thenComparing(Comparator.naturalOrder()));
    IO.println(noms);
    IO.println("a.b.c".split(".").length);
    IO.println("a,b,,".split(",").length);
}
