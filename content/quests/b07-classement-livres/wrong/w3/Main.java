import java.util.*;

public class Main {
    static List<Livre> parAnneeRecente(List<Livre> livres) {
        List<Livre> r = new ArrayList<>(livres);
        r.sort(Comparator.comparingInt(Livre::annee).reversed().thenComparing(Livre::titre));
        return r;
    }

    static List<Livre> parAuteurPuisPages(List<Livre> livres) {
        List<Livre> r = new ArrayList<>(livres);
        r.sort(Comparator.comparing(Livre::auteur).thenComparing(Livre::pages).reversed());
        return r;
    }
}
