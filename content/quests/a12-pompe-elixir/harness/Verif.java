
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static Class<?> leve(Runnable r) {
        try { r.run(); } catch (Throwable t) { return t.getClass(); }
        return null;
    }
    static void attendre(Class<?> type, Runnable r, String quand) {
        Class<?> got = leve(r);
        if (got == null) Check.fail(quand + " : aucune exception levée (attendu " + type.getSimpleName() + ").");
        Check.isTrue(got == type, quand + " : attendu " + type.getSimpleName() + ", obtenu " + got.getSimpleName() + ".");
    }
    static void verifier() {
        Check.test("Service normal", () -> {
            PompeElixir p = new PompeElixir(100, 3);
            Check.equal(30, p.servir(10), "Prix de 10 litres à 3");
            Check.equal(90, p.getStock(), "Stock après 10 litres");
            Check.equal(30, p.getRecette(), "Recette après 10 litres");
            Check.equal(9, p.servir(3), "Prix de 3 litres");
            Check.equal(39, p.getRecette(), "Recette cumulée");
        });
        Check.test("Vider exactement la cuve (limite)", () -> {
            PompeElixir p = new PompeElixir(5, 2);
            Check.equal(10, p.servir(5), "Prix de 5 litres");
            Check.equal(0, p.getStock(), "Stock vide");
            attendre(IllegalStateException.class, () -> p.servir(1), "servir(1) sur une cuve vide");
        });
        Check.test("Stock insuffisant : état inchangé", () -> {
            PompeElixir p = new PompeElixir(15, 4);
            attendre(IllegalStateException.class, () -> p.servir(40), "servir(40) avec 15 en stock");
            Check.equal(15, p.getStock(), "Stock après refus");
            Check.equal(0, p.getRecette(), "Recette après refus");
        });
        Check.test("Arguments invalides", () -> {
            PompeElixir p = new PompeElixir(10, 1);
            attendre(IllegalArgumentException.class, () -> p.servir(0), "servir(0)");
            attendre(IllegalArgumentException.class, () -> p.servir(-2), "servir(-2)");
            attendre(IllegalArgumentException.class, () -> new PompeElixir(-1, 2), "stock négatif");
            attendre(IllegalArgumentException.class, () -> new PompeElixir(10, 0), "prix nul");
            new PompeElixir(0, 1);
        });
        int[][] cas = {{0, 0}, {5, 1}, {15, 2}, {40, 2}, {85, 4}, {100, 2}, {95, 4}, {255, 6}, {1000, 20}};
        for (int[] c : cas)
            Check.test("compterBillets(" + c[0] + ")", () ->
                Check.equal(c[1], PompeElixir.compterBillets(c[0]), "Nombre minimal de billets"));
        Check.test("compterBillets : sommes impossibles", () -> {
            attendre(IllegalArgumentException.class, () -> PompeElixir.compterBillets(7), "7 pièces");
            attendre(IllegalArgumentException.class, () -> PompeElixir.compterBillets(-5), "-5 pièces");
        });
        Check.test("Encapsulation : champs privés", () -> {
            for (Field f : Check.playerClass("PompeElixir").getDeclaredFields())
                Check.isTrue(Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
        });
    }
}
