public class Poisson extends Animal implements Nageur {
    public Poisson(String nom) {
        super(nom);
    }

    public String nager() { return getNom() + " nage"; }
}
