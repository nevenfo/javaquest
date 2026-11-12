public abstract class Gardien {
    protected final String nom;

    protected Gardien(String nom) {
        this.nom = nom;
    }

    public abstract int force();

    public String presentation() {
        return "[" + nom + " | force " + force() + "]";
    }
}
