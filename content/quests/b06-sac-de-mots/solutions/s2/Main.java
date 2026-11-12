import java.util.*;

public class Main {
    static Map<String, Integer> compter(List<String> mots) {
        Map<String, Integer> m = new TreeMap<>();
        for (String mot : mots) m.put(mot, m.getOrDefault(mot, 0) + 1);
        return m;
    }

    static String plusFrequent(List<String> mots) {
        Map<String, Integer> m = compter(mots);
        String best = null;
        for (String k : m.keySet()) {
            if (best == null || m.get(k) > m.get(best)) best = k;
        }
        return best;
    }
}
