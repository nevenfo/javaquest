import java.util.*;

public class Atelier {
    private final Set<Armure> commandes = new LinkedHashSet<>();

    public boolean commander(Armure a) {
        if (a == null) throw new IllegalArgumentException("armure nulle");
        return commandes.add(a);
    }

    public int total() { return commandes.size(); }

    public List<Armure> commandes() { return new ArrayList<>(commandes); }
}
