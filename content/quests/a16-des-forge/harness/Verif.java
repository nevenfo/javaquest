import fr.javaquest.runner.Check;
import java.util.*;
class Verif {
    static void verifier() {
        Check.test("Valeurs dans les bornes, toutes présentes", () -> {
            Random r = new Random(7);
            Set<Integer> vues = new TreeSet<>();
            for (int i = 0; i < 2000; i++) {
                int v = Tirage.lancer(r, 6);
                Check.isTrue(v >= 1 && v <= 6, "Valeur hors bornes : " + v + " (attendu 1 à 6 inclus).");
                vues.add(v);
            }
            Check.equal(List.of(1, 2, 3, 4, 5, 6), new ArrayList<>(vues), "Valeurs obtenues en 2000 lancers");
        });
        Check.test("Dé à une face", () -> {
            for (int i = 0; i < 20; i++) Check.equal(1, Tirage.lancer(new Random(i), 1), "Dé à une face");
        });
        Check.test("Le Random reçu est utilisé", () -> {
            Random a = new Random(2026), b = new Random(2026);
            for (int i = 0; i < 30; i++) Check.equal(Tirage.lancer(a, 100), Tirage.lancer(b, 100), "Lancer n°" + (i + 1) + " avec deux Random de même graine");
        });
        Check.test("Faces invalides", () -> {
            boolean leve = false;
            try { Tirage.lancer(new Random(1), 0); } catch (IllegalArgumentException e) { leve = true; }
            Check.isTrue(leve, "faces = 0 doit lever IllegalArgumentException.");
        });
        Check.test("Compteur static partagé", () -> {
            int avant = Tirage.getLances();
            Tirage.lancer(new Random(1), 6);
            Tirage.lancer(new Random(2), 20);
            Check.equal(avant + 2, Tirage.getLances(), "Après deux lancers");
            try { Tirage.lancer(new Random(1), -3); } catch (IllegalArgumentException e) { }
            Check.equal(avant + 2, Tirage.getLances(), "Un lancer refusé ne compte pas");
        });
        Check.test("Le compteur est un champ static privé", () -> {
            int n = 0;
            for (var f : Check.playerClass("Tirage").getDeclaredFields()) {
                int m = f.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(m)) { n++; Check.isTrue(java.lang.reflect.Modifier.isPrivate(m), "Le compteur doit être private static."); }
            }
            Check.isTrue(n >= 1, "Il faut un champ static pour le compteur.");
        });
    }
}
