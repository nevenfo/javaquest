import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static Class<?> leve(Runnable r) {
        try { r.run(); } catch (Throwable t) { return t.getClass(); }
        return null;
    }
    static void refuse(Runnable r, String quand) {
        Class<?> got = leve(r);
        if (got == null) Check.fail(quand + " : aucune exception levée (attendu IllegalArgumentException).");
        Check.isTrue(got == IllegalArgumentException.class, quand + " : attendu IllegalArgumentException, obtenu " + got.getSimpleName() + ".");
    }
    static void verifier() {
        Check.test("Accesseurs et toString", () -> {
            Alliage a = new Alliage("bronze", 88, 12);
            Check.equal("bronze", a.getNom(), "getNom()");
            Check.equal(88, a.getCuivre(), "getCuivre()");
            Check.equal(12, a.getEtain(), "getEtain()");
            Check.equal("Alliage bronze (88% cuivre, 12% etain)", a.toString(), "toString()");
            Check.equal("Alliage pur (100% cuivre, 0% etain)", new Alliage("pur", 100, 0).toString(), "toString() d'un alliage limite");
        });
        Check.test("Alliages limites acceptés", () -> {
            new Alliage("cuivre", 100, 0);
            new Alliage("etain", 0, 100);
            new Alliage("moitie", 50, 50);
        });
        Check.test("Nom invalide", () -> {
            refuse(() -> new Alliage(null, 80, 20), "nom null");
            refuse(() -> new Alliage("", 80, 20), "nom vide");
            refuse(() -> new Alliage("   ", 80, 20), "nom blanc");
        });
        Check.test("Proportions invalides", () -> {
            refuse(() -> new Alliage("a", 60, 30), "somme 90");
            refuse(() -> new Alliage("a", 70, 40), "somme 110");
            refuse(() -> new Alliage("a", -10, 110), "cuivre négatif (somme 100)");
            refuse(() -> new Alliage("a", 110, -10), "étain négatif (somme 100)");
        });
        Check.test("estBronze() : 80 % de cuivre ou plus", () -> {
            Check.isTrue(new Alliage("a", 80, 20).estBronze(), "80 % de cuivre : bronze.");
            Check.isTrue(new Alliage("a", 100, 0).estBronze(), "100 % de cuivre : bronze.");
            Check.isTrue(!new Alliage("a", 79, 21).estBronze(), "79 % de cuivre : pas du bronze.");
            Check.isTrue(!new Alliage("a", 0, 100).estBronze(), "0 % de cuivre : pas du bronze.");
        });
        Check.test("Encapsulation : champs private final", () -> {
            Class<?> c = Check.playerClass("Alliage");
            int n = 0;
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                n++;
                Check.isTrue(Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
                Check.isTrue(Modifier.isFinal(f.getModifiers()), "Le champ " + f.getName() + " doit être final.");
            }
            Check.isTrue(n >= 3, "Il faut au moins trois champs d'instance (nom, cuivre, étain).");
        });
    }
}
