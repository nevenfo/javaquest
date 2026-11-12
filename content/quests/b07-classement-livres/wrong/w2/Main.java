import java.util.*;

public class Main {
    static List<Livre> parAnneeRecente(List<Livre> livres) {
        livres.sort(Comparator.comparingInt(Livre::annee).reversed().thenComparing(Livre::titre));
        return livres;
    }

    static List<Livre> parAuteurPuisPages(List<Livre> livres) {
        livres.sort(Comparator.comparing(Livre::auteur).thenComparing(Comparator.comparingInt(Livre::pages).reversed()));
        return livres;
    }
}
