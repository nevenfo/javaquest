import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] mots = {"pique", "as", "roi", "as", "dame", "roi", "as", "fou"};

        Set<String> arrivee = new LinkedHashSet<>(Arrays.asList(mots));
        Set<String> alpha = new TreeSet<>(Arrays.asList(mots));
        Set<String> vus = new HashSet<>(Arrays.asList(mots));
        System.out.println(arrivee);
        System.out.println(alpha);
        System.out.println(vus.size() + " " + vus.contains("dame"));

        Map<String, Integer> compte = new TreeMap<>();
        for (String m : mots) {
            compte.merge(m, 1, Integer::sum);
        }
        System.out.println(compte);

        Map<String, Integer> lettres = new LinkedHashMap<>();
        for (String m : mots) {
            lettres.put(m, lettres.getOrDefault(m, 0) + m.length());
        }
        System.out.println(lettres);

        List<String> rang = new ArrayList<>(arrivee);
        rang.sort(Comparator.comparing(String::length).thenComparing(Comparator.reverseOrder()));
        System.out.println(rang);

        LinkedList<String> file = new LinkedList<>(arrivee);
        file.addFirst("joker");
        file.removeLast();
        System.out.println(file + " " + file.getFirst());

        int[] cotes = {5, 2, 9, 2};
        Arrays.sort(cotes);
        System.out.println(Arrays.toString(cotes) + " " + compte.get("valet") + " " + compte.getOrDefault("valet", -1));
    }
}
