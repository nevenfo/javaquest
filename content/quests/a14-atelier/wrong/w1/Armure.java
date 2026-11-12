import java.util.Objects;

public class Armure {
    private final String nom;
    private final int niveau;

    public Armure(String nom, int niveau) {
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("nom manquant");
        if (niveau < 1 || niveau > 10) throw new IllegalArgumentException("niveau hors 1..10");
        this.nom = nom;
        this.niveau = niveau;
    }

    public String getNom() { return nom; }
    public int getNiveau() { return niveau; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Armure a && nom.equals(a.nom) && niveau == a.niveau;
    }


    @Override
    public String toString() { return "Armure[" + nom + ", niveau " + niveau + "]"; }
}
