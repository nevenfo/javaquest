import java.util.List;

public class Main {
    static int attaqueTotale(List<Monstre> horde) {
        int total = 0;
        for (Monstre m : horde) total += m.attaque();
        return total;
    }

    static String cris(List<Monstre> horde) {
        String s = "";
        for (Monstre m : horde) s += m.cri() + " ";
        return s;
    }

    static Monstre plusFort(List<Monstre> horde) {
        Monstre best = null;
        for (Monstre m : horde) if (best == null || m.attaque() >= best.attaque()) best = m;
        return best;
    }
}
