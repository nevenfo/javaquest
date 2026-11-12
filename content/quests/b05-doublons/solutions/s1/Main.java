import java.util.*;

public class Main {
    static String premierDoublon(List<String> mots) {
        Set<String> vus = new HashSet<>();
        for (String m : mots) {
            if (!vus.add(m)) return m;
        }
        return null;
    }

    static List<String> sansDoublons(List<String> mots) {
        return new ArrayList<>(new LinkedHashSet<>(mots));
    }

    static List<String> alphabetique(List<String> mots) {
        return new ArrayList<>(new TreeSet<>(mots));
    }
}
