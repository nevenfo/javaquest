class VersCible implements Strategie {
    public String choisir(Compagnon c) {
        if (c.x() < c.cibleX()) return "droite";
        if (c.x() > c.cibleX()) return "gauche";
        return null;
    }
}

class LongeMurDroit implements Strategie {
    private static final String[] ORDRE = {"haut", "droite", "bas", "gauche"};

    public String choisir(Compagnon c) {
        int cap = java.util.Arrays.asList(ORDRE).indexOf(c.cap());
        for (int quart : new int[]{1, 0, 3, 2}) {
            String dir = ORDRE[(cap + quart) % 4];
            if (!c.devantMur(dir)) return dir;
        }
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        Compagnon c = Compagnon.demarrer();
        Pilote.conduire(c, new LongeMurDroit(), 100);
    }
}
