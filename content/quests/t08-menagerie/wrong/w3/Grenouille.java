public class Grenouille extends Animal implements Marcheur, Nageur {
    public Grenouille(String nom) {
        super(nom);
    }

    public String marcher() { return getNom() + " saute"; }

    public String nager() { return getNom() + " nage"; }
}
