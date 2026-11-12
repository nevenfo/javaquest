public abstract class Creature {
    private final String nom;
    protected Creature(String nom) { this.nom = nom; }
    public String nom() { return nom; }
}
