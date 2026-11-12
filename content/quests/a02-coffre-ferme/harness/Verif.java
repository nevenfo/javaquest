
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static void verifier() {
        Check.test("Les champs sont private", () -> {
            Class<?> c = Check.playerClass("Rivet");
            int n = 0;
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                n++;
                Check.isTrue(Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
            }
            Check.equal(2, n, "Nombre de champs d'instance");
        });
        Check.test("Les champs sont final", () -> {
            for (Field f : Check.playerClass("Rivet").getDeclaredFields())
                if (!Modifier.isStatic(f.getModifiers()))
                    Check.isTrue(Modifier.isFinal(f.getModifiers()), "Le champ " + f.getName() + " doit être final : un rivet posé ne bouge plus.");
        });
        Check.test("Pas de mutateur setX / setY", () -> {
            for (Method m : Check.playerClass("Rivet").getDeclaredMethods())
                Check.isTrue(!m.getName().startsWith("set"), "La méthode " + m.getName() + " permettrait de modifier le rivet.");
        });
        int[][] cas = {{3, 4}, {0, 0}, {-5, 12}};
        for (int[] c : cas) {
            Check.test("getX et getY pour (" + c[0] + ", " + c[1] + ")", () -> {
                Rivet r = new Rivet(c[0], c[1]);
                Check.equal(c[0], r.getX(), "getX()");
                Check.equal(c[1], r.getY(), "getY()");
            });
        }
    }
}
