import java.util.*;

public class Main {
    static List<Chasseur> lire(List<String> lignes) {
        List<Chasseur> r = new ArrayList<>();
        for (String l : lignes) {
            if (l.isBlank()) continue;
            String[] p = l.split(":");
            r.add(new Chasseur(p[0].trim(), Integer.parseInt(p[1].trim())));
        }
        return r;
    }

    static List<String> classement(List<String> lignes) {
        List<Chasseur> l = lire(lignes);
        l.sort(Comparator.comparingInt(Chasseur::prises).reversed().thenComparing(Chasseur::nom));
        List<String> out = new ArrayList<>();
        for (Chasseur c : l) out.add(c.nom() + " (" + c.prises() + ")");
        return out;
    }
}
