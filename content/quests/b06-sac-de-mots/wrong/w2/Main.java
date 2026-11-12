import java.util.*;

public class Main {
    static Map<String, Integer> compter(List<String> mots) {
        Map<String, Integer> m = new HashMap<>();
        for (String mot : mots) m.merge(mot, 1, Integer::sum);
        return m;
    }

    static String plusFrequent(List<String> mots) {
        String best = null;
        int max = 0;
        for (Map.Entry<String, Integer> e : compter(mots).entrySet()) {
            if (e.getValue() >= max) {
                best = e.getKey();
                max = e.getValue();
            }
        }
        return best;
    }
}
