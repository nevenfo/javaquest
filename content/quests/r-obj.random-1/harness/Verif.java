import fr.javaquest.runner.Check;
import java.util.*;
class Verif {
    static void verifier() {
        List<String> jeu = List.of("A", "B", "C", "D", "E", "F", "G", "H");
        Check.test("n cartes distinctes issues du jeu", () -> {
            for (int s = 0; s < 30; s++) {
                List<String> r = Pioche.tirer(new Random(s), jeu, 5);
                Check.equal(5, r.size(), "Nombre de cartes tirées");
                Check.equal(5, new HashSet<>(r).size(), "Cartes distinctes (graine " + s + ")");
                Check.isTrue(jeu.containsAll(r), "Une carte tirée n'est pas dans le jeu.");
            }
        });
        Check.test("Toutes les cartes peuvent sortir", () -> {
            Set<String> vues = new HashSet<>();
            for (int s = 0; s < 60; s++) vues.addAll(Pioche.tirer(new Random(s), jeu, 2));
            Check.equal(8, vues.size(), "Cartes vues en 60 tirages de 2");
        });
        Check.test("Cas limites", () -> {
            Check.equal(List.of(), Pioche.tirer(new Random(1), jeu, 0), "n = 0");
            Check.equal(8, Pioche.tirer(new Random(1), jeu, 8).size(), "Tout le jeu");
            for (int n : new int[]{9, -1}) {
                boolean leve = false;
                try { Pioche.tirer(new Random(1), jeu, n); } catch (IllegalArgumentException e) { leve = true; }
                Check.isTrue(leve, "n = " + n + " doit lever IllegalArgumentException.");
            }
        });
        Check.test("Liste d'origine intacte, même graine même résultat", () -> {
            List<String> src = new ArrayList<>(jeu);
            List<String> a = Pioche.tirer(new Random(9), src, 4);
            Check.equal(jeu, src, "Liste d'origine");
            Check.equal(a, Pioche.tirer(new Random(9), src, 4), "Deux tirages avec la même graine");
        });
    }
}
