public class Outil {
    private String nom;
    private int durabilite;

    public Outil(String nom, int durabilite) {
        this.nom = nom;
        this.durabilite = durabilite;
    }

    public String getNom() { return nom; }

    public int getDurabilite() { return durabilite; }

    public void utiliser() {
        durabilite--;
    }
}
