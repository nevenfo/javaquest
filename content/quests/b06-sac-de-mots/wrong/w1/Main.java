import java.util.*;

public class Main {
    static Map<String, Integer> compter(List<String> mots) {
        Map<String, Integer> m = new HashMap<>();
        for (String mot : mots) m.put(mot, 1);
        return m;
    }

    static String plusFrequent(List<String> mots) {
        String best = null;
        int max = 0;
        for (Map.Entry<String, Integer> e : compter(mots).entrySet()) {
            int n = e.getValue();
            if (n > max || (n == max && e.getKey().compareTo(best) < 0)) {
                best = e.getKey();
                max = n;
            }
        }
        return best;
    }
}
