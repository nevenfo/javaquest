
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Valeurs dans les bornes, toutes présentes (1 à 6)", () -> {
            Random r = new Random(7);
            Set<Integer> vues = new TreeSet<>();
            for (int i = 0; i < 3000; i++) {
                int v = Gemme.aleatoire(r, 1, 6).valeur();
                Check.isTrue(v >= 1 && v <= 6, "Valeur hors bornes : " + v + " (attendu entre 1 et 6 inclus).");
                vues.add(v);
            }
            Check.equal(List.of(1, 2, 3, 4, 5, 6), new ArrayList<>(vues), "Valeurs obtenues en 3000 tirages (les bornes doivent sortir)");
        });
        Check.test("Plage négative [-3, 3]", () -> {
            Random r = new Random(11);
            Set<Integer> vues = new TreeSet<>();
            for (int i = 0; i < 3000; i++) {
                int v = Gemme.aleatoire(r, -3, 3).valeur();
                Check.isTrue(v >= -3 && v <= 3, "Valeur hors bornes : " + v);
                vues.add(v);
            }
            Check.equal(7, vues.size(), "Nombre de valeurs distinctes obtenues");
        });
        Check.test("min = max", () -> {
            for (int i = 0; i < 20; i++)
                Check.equal(5, Gemme.aleatoire(new Random(i), 5, 5).valeur(), "Gemme de valeur imposée");
        });
        Check.test("Le Random reçu est bien utilisé (même graine, mêmes gemmes)", () -> {
            Random a = new Random(2026), b = new Random(2026);
            for (int i = 0; i < 50; i++)
                Check.equal(Gemme.aleatoire(a, 10, 99), Gemme.aleatoire(b, 10, 99), "Gemme n°" + (i + 1) + " avec deux Random de même graine");
        });
        Check.test("Les tirages varient", () -> {
            Random r = new Random(3);
            Set<Integer> vues = new HashSet<>();
            for (int i = 0; i < 100; i++) vues.add(Gemme.aleatoire(r, 0, 1000).valeur());
            Check.isTrue(vues.size() > 50, "100 tirages entre 0 et 1000 ne donnent que " + vues.size() + " valeurs différentes : le tirage semble figé.");
        });
        Check.test("min > max", () -> {
            boolean leve = false;
            try { Gemme.aleatoire(new Random(1), 6, 1); } catch (IllegalArgumentException e) { leve = true; }
            Check.isTrue(leve, "min > max doit lever IllegalArgumentException.");
        });
    }
}
