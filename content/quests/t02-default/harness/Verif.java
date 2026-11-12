
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static void verifier() {
        Check.test("presentation est une méthode default, creer est static", () -> {
            Class<?> c = Check.playerClass("Combattant");
            Check.isTrue(c.isInterface(), "Combattant doit être une interface.");
            Check.isTrue(c.getMethod("presentation").isDefault(), "presentation() doit être `default`.");
            Check.isTrue(Modifier.isStatic(c.getMethod("creer", String.class, int.class).getModifiers()), "creer(...) doit être `static`.");
        });
        Check.test("Une classe n'a besoin que de nom() et force()", () -> {
            Combattant c = new Combattant() {
                public String nom() { return "Rex"; }
                public int force() { return 7; }
            };
            Check.equal("Rex (force 7)", c.presentation(), "presentation()");
        });
        Check.test("La méthode default peut être redéfinie", () -> {
            Combattant c = new Combattant() {
                public String nom() { return "Zed"; }
                public int force() { return 1; }
                public String presentation() { return "Zed le sage"; }
            };
            Check.equal("Zed le sage", c.presentation(), "presentation() redéfinie");
        });
        String[] noms = {"Aria", "Brum", ""};
        int[] forces = {12, 0, -3};
        for (int i = 0; i < noms.length; i++) {
            String n = noms[i]; int f = forces[i];
            Check.test("Combattant.creer(\"" + n + "\", " + f + ")", () -> {
                Combattant c = Combattant.creer(n, f);
                Check.isTrue(c != null, "creer ne doit pas renvoyer null.");
                Check.equal(n, c.nom(), "nom()");
                Check.equal(f, c.force(), "force()");
                Check.equal(n + " (force " + f + ")", c.presentation(), "presentation()");
            });
        }
    }
}
