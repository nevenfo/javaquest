import java.util.Objects;

public class Moule {
    private String nom;
    private int capacite;

    public Moule(String nom, int capacite) {
        this.nom = nom;
        this.capacite = capacite;
    }

    public String getNom() {
        return nom;
    }

    public int getCapacite() {
        return capacite;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Moule)) {
            return false;
        }
        Moule autre = (Moule) o;
        return nom == autre.nom && capacite == autre.capacite;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, capacite);
    }

    @Override
    public String toString() {
        return "Moule[" + nom + ", " + capacite + "]";
    }
}
