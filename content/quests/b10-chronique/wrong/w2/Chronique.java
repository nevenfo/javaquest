import java.util.*;

public class Chronique {
    private final int capacite;
    private final LinkedList<String> evenements = new LinkedList<>();

    public Chronique(int capacite) { this.capacite = capacite; }

    public void noter(String evenement) {
        evenements.addFirst(evenement);
        if (evenements.size() > capacite) evenements.removeLast();
    }

    public List<String> recents() { return new ArrayList<>(evenements); }

    public List<String> parLongueur() {
        evenements.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        return new ArrayList<>(evenements);
    }
}
