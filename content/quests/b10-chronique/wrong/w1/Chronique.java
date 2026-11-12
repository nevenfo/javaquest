import java.util.*;

public class Chronique {
    private final int capacite;
    private final List<String> evenements = new ArrayList<>();

    public Chronique(int capacite) { this.capacite = capacite; }

    public void noter(String evenement) {
        evenements.add(0, evenement);
        if (evenements.size() > capacite) evenements.remove(evenements.size() - 1);
    }

    public List<String> recents() { return new ArrayList<>(evenements); }

    public List<String> parLongueur() {
        List<String> l = new ArrayList<>(evenements);
        l.sort(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
        return l;
    }
}
