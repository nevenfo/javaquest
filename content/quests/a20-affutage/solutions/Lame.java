public class Lame {
    private final String nom;
    private int tranchant;

    public Lame(String nom, int tranchant) {
        this.nom = nom;
        this.tranchant = tranchant;
    }

    public String getNom() { return nom; }

    public int getTranchant() { return tranchant; }

    public void affuter(int points) {
        tranchant = Math.min(100, tranchant + points);
    }

    public boolean estAffutee(int seuil) {
        return tranchant >= seuil;
    }
}
