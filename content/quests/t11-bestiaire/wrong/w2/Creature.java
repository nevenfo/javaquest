public class Creature {
    private final String nom;
    public Creature(String nom) { this.nom = nom; }
    public String nom() { return nom; }
    public int force() { return 0; }
    public String presenter() { return nom + " (force " + force() + ")"; }
}
