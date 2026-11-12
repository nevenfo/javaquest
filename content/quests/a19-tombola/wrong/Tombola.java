import java.util.*;

public class Tombola {
    private final Random rng;
    private final List<String> lots;

    public Tombola(Random rng, List<String> lots) {
        this.rng = rng;
        this.lots = new ArrayList<>(lots);
    }

    public String tirer() {
        if (lots.isEmpty()) throw new IllegalStateException("plus de lot");
        return lots.get(rng.nextInt(lots.size()));
    }

    public int restants() { return lots.size(); }

    @Override
    public String toString() { return "Tombola" + lots; }
}
