public abstract class Animal {
    private final String nom;

    protected Animal(String nom) {
        this.nom = nom;
    }

    public String getNom() { return nom; }
}
