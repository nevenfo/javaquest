import java.util.*;

public class Chronique {
    private final int max;
    private final LinkedList<String> liste = new LinkedList<>();

    public Chronique(int capacite) { max = capacite; }

    public void noter(String e) {
        liste.add(0, e);
        while (liste.size() > max) liste.removeLast();
    }

    public List<String> recents() { return new LinkedList<>(liste); }

    public List<String> parLongueur() {
        List<String> l = new ArrayList<>(liste);
        l.sort((a, b) -> a.length() != b.length() ? b.length() - a.length() : a.compareTo(b));
        return l;
    }
}
