public class Canard extends Animal implements Marcheur, Nageur, Volant {
    public Canard(String nom) {
        super(nom);
    }

    public String marcher() { return getNom() + " marche"; }

    public String nager() { return getNom() + " nage"; }

    public String voler() { return getNom() + " vole"; }
}
