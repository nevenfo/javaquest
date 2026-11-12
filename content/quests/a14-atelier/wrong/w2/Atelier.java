import java.util.*;

public class Atelier {
    private final List<Armure> liste = new ArrayList<>();

    public boolean commander(Armure a) {
        if (a == null) throw new IllegalArgumentException("armure nulle");
        if (liste.contains(a)) return false;
        return liste.add(a);
    }

    public int total() { return liste.size(); }

    public List<Armure> commandes() { return liste; }
}
