import java.util.*;

public class Main {
    static List<String> mots(String texte) {
        String t = texte.strip();
        if (t.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(t.split("\\s+")));
    }

    static int distincts(String texte) {
        return mots(texte).size();
    }

    static Map<String, Integer> frequences(String texte) {
        Map<String, Integer> m = new HashMap<>();
        for (String mot : mots(texte)) m.merge(mot, 1, Integer::sum);
        return m;
    }
}
