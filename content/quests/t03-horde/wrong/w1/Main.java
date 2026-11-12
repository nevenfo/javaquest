import java.util.List;

public class Main {
    static int attaqueTotale(List<Monstre> horde) {
        int total = 0;
        for (Monstre m : horde) {
            if (m instanceof Gobelin) total += 3;
            else if (m instanceof Dragon d) total += d.attaque();
        }
        return total;
    }

    static String cris(List<Monstre> horde) {
        StringBuilder sb = new StringBuilder();
        for (Monstre m : horde) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(m.cri());
        }
        return sb.toString();
    }

    static Monstre plusFort(List<Monstre> horde) {
        Monstre best = null;
        for (Monstre m : horde) if (best == null || m.attaque() > best.attaque()) best = m;
        return best;
    }
}
