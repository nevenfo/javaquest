import java.util.*;

public class Main {
    static Map<String, List<String>> indexer(List<String> fiches) {
        Map<String, Set<String>> tmp = new TreeMap<>();
        for (String fiche : fiches) {
            if (fiche.isBlank()) continue;
            String[] p = fiche.split(";");
            String titre = p[0].strip();
            if (p.length < 2) continue;
            for (String t : p[1].split(",")) {
                String tag = t;
                if (tag.isEmpty()) continue;
                tmp.computeIfAbsent(tag, k -> new TreeSet<>()).add(titre);
            }
        }
        Map<String, List<String>> index = new TreeMap<>();
        for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
            index.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return index;
    }

    static List<String> classement(Map<String, List<String>> index) {
        List<String> tags = new ArrayList<>(index.keySet());
        tags.sort(Comparator.<String>comparingInt(t -> index.get(t).size()).reversed().thenComparing(Comparator.naturalOrder()));
        List<String> r = new ArrayList<>();
        for (String t : tags) r.add(t + " (" + index.get(t).size() + ")");
        return r;
    }
}
