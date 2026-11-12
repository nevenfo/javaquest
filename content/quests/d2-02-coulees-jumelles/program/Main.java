class Fiole {
    static int total = 0;
    int niveau;

    Fiole(int niveau) {
        this.niveau = niveau;
        total++;
    }
}

void main() {
    Fiole a = new Fiole(3);
    Fiole b = a;
    Fiole c = new Fiole(3);
    b.niveau = b.niveau + 2;
    c.niveau++;
    IO.println(a.niveau + " " + c.niveau);
    IO.println(a == b);
    IO.println(a == c);
    int[] lots = {1, 2};
    int[] copie = lots;
    copie[0] = 9;
    IO.println(lots[0] + " " + Fiole.total);
    Fiole d = c;
    c = new Fiole(1);
    d.niveau += 10;
    IO.println(c.niveau + " " + d.niveau + " " + Fiole.total);
}
