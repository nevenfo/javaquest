public abstract class Creature {
    private final String nom;
    public Creature(String nom) { this.nom = nom; }
    public String nom() { return nom; }
    public abstract int force();
    public String presenter() { return nom + " (force " + force() + ")"; }
}
