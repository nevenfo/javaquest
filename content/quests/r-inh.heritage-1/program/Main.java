import java.util.*;

interface Marcheur { default String marche() { return "pas"; } }
interface Nageur { default String nage() { return "brasse"; } }

abstract class Animal {
    String nom;
    Animal(String nom) { this.nom = nom; }
    abstract String cri();
    String decrire() { return nom + " fait " + cri(); }
}

class Chien extends Animal implements Marcheur {
    Chien() { super("Chien"); }
    String cri() { return "wouf"; }
}

class Canard extends Animal implements Marcheur, Nageur {
    Canard() { super("Canard"); }
    String cri() { return "coin"; }
    String decrire() { return super.decrire() + " et " + nage(); }
    public String marche() { return "dandine"; }
}

void main() {
    Animal a = new Canard();
    IO.println(a.decrire());
    Chien c = new Chien();
    IO.println(c.decrire() + " / " + c.marche());
    Marcheur m = new Canard();
    IO.println(m.marche());
    Animal[] zoo = { c, a };
    int n = 0;
    for (Animal x : zoo) if (x instanceof Nageur) n++;
    IO.println(n);
}
