public abstract class Animal implements Nageur {
    private final String nom;

    protected Animal(String nom) {
        this.nom = nom;
    }

    public String nager() { return nom + " nage"; }

    public String getNom() { return nom; }
}
