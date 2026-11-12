import java.util.*;

public class Main {
    static List<String> mots(String texte) {
        return new ArrayList<>(Arrays.asList(texte.toLowerCase().split(" ")));
    }

    static int distincts(String texte) {
        return new HashSet<>(mots(texte)).size();
    }

    static Map<String, Integer> frequences(String texte) {
        Map<String, Integer> m = new HashMap<>();
        for (String mot : mots(texte)) m.merge(mot, 1, Integer::sum);
        return m;
    }
}
