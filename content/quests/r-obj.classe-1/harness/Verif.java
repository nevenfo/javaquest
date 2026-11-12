
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static void verifier() {
        Check.test("Champs private final", () -> {
            for (Field f : Check.playerClass("Fiole").getDeclaredFields())
                if (!Modifier.isStatic(f.getModifiers()))
                    Check.isTrue(Modifier.isPrivate(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), "Le champ " + f.getName() + " doit être private final.");
        });
        String[] liq = {"eau", "lave", ""};
        int[] cont = {5, 0, 12};
        for (int i = 0; i < liq.length; i++) {
            String l = liq[i]; int c = cont[i];
            Check.test("Fiole(\"" + l + "\", " + c + ")", () -> {
                Fiole f = new Fiole(l, c);
                Check.equal(l, f.getLiquide(), "getLiquide()");
                Check.equal(c, f.getContenance(), "getContenance()");
                Check.equal(c == 0, f.estVide(), "estVide()");
            });
        }
        Check.test("Deux fioles indépendantes", () -> {
            Fiole a = new Fiole("x", 1), b = new Fiole("y", 2);
            Check.equal("x", a.getLiquide(), "a"); Check.equal(2, b.getContenance(), "b");
        });
    }
}
