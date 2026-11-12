import java.util.*;

public class Main {
    static List<String> sansDoublons(List<String> objets) {
        Set<String> vus = new HashSet<>();
        List<String> r = new ArrayList<>();
        for (String o : objets) if (vus.add(o)) r.add(o);
        return r;
    }

    static Map<String, List<Integer>> positions(List<String> objets) {
        Map<String, List<Integer>> m = new TreeMap<>();
        for (int i = 0; i < objets.size(); i++) {
            String o = objets.get(i);
            if (!m.containsKey(o)) m.put(o, new ArrayList<>());
            m.get(o).add(i);
        }
        return m;
    }

    static void retirerTous(List<String> objets, String cible) {
        Iterator<String> it = objets.iterator();
        while (it.hasNext()) if (it.next().equals(cible)) it.remove();
    }
}
