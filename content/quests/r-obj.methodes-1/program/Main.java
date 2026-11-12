class Pot {
    private int niveau;
    private final String nom;

    Pot(String nom, int niveau) {
        this.nom = nom;
        this.niveau = niveau;
    }

    void verser(int cl) { niveau += cl; }

    Pot plusPlein(int cl) { return new Pot(nom, niveau + cl); }

    int niveau() { return niveau; }

    @Override
    public String toString() { return nom + "(" + niveau + ")"; }
}

void main() {
    Pot a = new Pot("eau", 10);
    Pot b = a;
    b.verser(5);
    Pot c = a.plusPlein(20);
    IO.println(a);
    IO.println(c);
    IO.println(a.niveau() + c.niveau());
    IO.println("Pots : " + a + " et " + c);
}
