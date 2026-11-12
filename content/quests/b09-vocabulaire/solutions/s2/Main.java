import java.util.*;

public class Main {
    static List<String> mots(String texte) {
        List<String> r = new ArrayList<>();
        for (String s : texte.toLowerCase().split("\\s+")) if (!s.isEmpty()) r.add(s);
        return r;
    }

    static int distincts(String texte) {
        Set<String> s = new TreeSet<>();
        s.addAll(mots(texte));
        return s.size();
    }

    static Map<String, Integer> frequences(String texte) {
        Map<String, Integer> m = new TreeMap<>();
        for (String mot : mots(texte)) m.put(mot, m.getOrDefault(mot, 0) + 1);
        return m;
    }
}
