interface Bete { String bruit(); }

class Chat implements Bete {
    public String bruit() { return "miaou"; }
}

class Chien implements Bete {
    public String bruit() { return "ouaf"; }
}

class Chiot extends Chien {
    public String bruit() { return super.bruit() + "!"; }
}

void main() {
    java.util.List<Bete> l = java.util.List.of(new Chat(), new Chien(), new Chiot());
    for (Bete b : l) IO.println(b.bruit());
    Chien c = new Chiot();
    IO.println(c.bruit() + c.bruit().length());
}
