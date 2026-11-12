import java.util.Objects;

public class Lingot {
    private final String metal;
    private final int poids;

    public Lingot(String metal, int poids) {
        this.metal = metal;
        this.poids = poids;
    }

    public String getMetal() { return metal; }
    public int getPoids() { return poids; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Lingot l && Objects.equals(metal, l.metal) && poids == l.poids;
    }

    @Override
    public String toString() { return "Lingot(" + metal + ", " + poids + ")"; }
}
