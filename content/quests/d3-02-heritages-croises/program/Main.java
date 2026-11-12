interface Blason {
    String nom();

    default String cri() {
        return "Pour " + nom();
    }
}

class Maison implements Blason {
    public String nom() { return "la Maison"; }

    String fierte() { return "or"; }
}

class Lignee extends Maison {
    public String nom() { return "la Lignée"; }

    public String cri() { return super.cri() + " !"; }

    String fierte() { return super.fierte() + " et argent"; }
}

class Clan implements Blason {
    public String nom() { return "le Clan"; }

    public String cri() { return Blason.super.cri().toUpperCase(); }
}

void annoncer(Blason b) {
    IO.println("Blason : " + b.cri());
}

void annoncer(Maison m) {
    IO.println("Maison : " + m.fierte());
}

void main() {
    Blason a = new Maison();
    Blason b = new Lignee();
    Maison c = new Lignee();
    Blason d = new Clan();
    IO.println(a.cri());
    IO.println(b.cri());
    IO.println(c.fierte());
    IO.println(d.cri());
    annoncer(b);
    annoncer(c);
    annoncer((Blason) c);
}
