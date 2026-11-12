public abstract class Instrument {
    private final String nom;

    protected Instrument(String nom) { this.nom = nom; }

    public String nom() { return nom; }

    public abstract String son();

    public String jouer() { return nom + " : " + son(); }
}
