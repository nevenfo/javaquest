public class Epee {
    public static final int LAME_MAX = 120;
    private static int forgees = 0;

    private final String nom;

    public Epee(String nom) {
        this.nom = nom;
        forgees = 1;
    }

    public String getNom() { return nom; }

    public static int getForgees() { return forgees; }
}
