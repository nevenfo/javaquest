public class Poisson extends Animal implements Marcheur, Nageur {
    public Poisson(String nom) {
        super(nom);
    }

    public String marcher() { return getNom() + " marche"; }

    public String nager() { return getNom() + " nage"; }
}
