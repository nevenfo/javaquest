
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static String t(List<Livre> l) {
        StringBuilder sb = new StringBuilder();
        for (Livre x : l) sb.append(x.titre()).append(' ');
        return sb.toString().trim();
    }
    static List<Livre> jeu() {
        return new ArrayList<>(List.of(
            new Livre("Cendres", "Moru", 1990, 200),
            new Livre("Ambre", "Alda", 2020, 90),
            new Livre("Brume", "Moru", 2020, 350),
            new Livre("Aube", "Alda", 1990, 90),
            new Livre("Zéphyr", "Moru", 2020, 120)));
    }
    static void verifier() {
        Check.test("parAnneeRecente", () -> {
            Check.equal("Ambre Brume Zéphyr Aube Cendres", t(Main.parAnneeRecente(jeu())), "Titres triés (année décroissante, puis titre)");
        });
        Check.test("parAuteurPuisPages", () -> {
            Check.equal("Ambre Aube Brume Cendres Zéphyr", t(Main.parAuteurPuisPages(jeu())), "Titres triés (auteur, puis pages décroissantes)");
        });
        Check.test("Egalité de pages : ordre stable avec auteur", () -> {
            List<Livre> l = new ArrayList<>(List.of(new Livre("B", "X", 2000, 10), new Livre("A", "X", 2001, 10), new Livre("C", "A", 1999, 5)));
            Check.equal("C B A", t(Main.parAuteurPuisPages(l)), "Auteur A, puis X (pages égales : ordre d'origine conservé)");
        });
        Check.test("La liste d'origine ne change pas", () -> {
            List<Livre> src = jeu();
            String avant = t(src);
            Main.parAnneeRecente(src);
            Main.parAuteurPuisPages(src);
            Check.equal(avant, t(src), "Liste d'origine après les tris");
        });
        Check.test("Listes vide et à un élément", () -> {
            Check.equal(0, Main.parAnneeRecente(new ArrayList<>()).size(), "parAnneeRecente([])");
            Check.equal(0, Main.parAuteurPuisPages(new ArrayList<>()).size(), "parAuteurPuisPages([])");
            Check.equal("Seul", t(Main.parAnneeRecente(new ArrayList<>(List.of(new Livre("Seul", "S", 1, 1))))), "Un seul livre");
        });
        Check.test("Un livre reste le même objet", () -> {
            Livre a = new Livre("A", "Z", 2000, 1);
            Check.isTrue(Main.parAnneeRecente(new ArrayList<>(List.of(a))).get(0) == a, "Les livres triés sont les mêmes objets (pas de copie).");
        });
    }
}
