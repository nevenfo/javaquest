public class Ancetre {
    protected final String nom;
    protected final int annee;

    public Ancetre(String nom, int annee) {
        this.nom = nom;
        this.annee = annee;
    }

    public String devise() {
        return "Honneur";
    }

    public String presenter() {
        return nom + " (" + annee + ") : " + devise();
    }
}
