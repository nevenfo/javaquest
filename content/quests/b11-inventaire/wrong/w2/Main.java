import java.util.*;

public class Main {
    static List<String> sansDoublons(List<String> objets) {
        return new ArrayList<>(new TreeSet<>(objets));
    }

    static Map<String, List<Integer>> positions(List<String> objets) {
        Map<String, List<Integer>> m = new HashMap<>();
        for (int i = 0; i < objets.size(); i++) m.computeIfAbsent(objets.get(i), k -> new ArrayList<>()).add(i);
        return m;
    }

    static void retirerTous(List<String> objets, String cible) {
        objets.removeIf(x -> x.equals(cible));
    }
}
