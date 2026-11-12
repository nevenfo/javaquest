import java.util.*;

public class Main {
    static String premierDoublon(List<String> mots) {
        for (int i = 0; i < mots.size(); i++) {
            if (mots.indexOf(mots.get(i)) != i || mots.lastIndexOf(mots.get(i)) != i) return mots.get(i);
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
