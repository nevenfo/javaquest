
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static void verifier() {
        Check.test("Constante LAME_MAX", () -> {
            Field f = Check.playerClass("Epee").getDeclaredField("LAME_MAX");
            int m = f.getModifiers();
            Check.isTrue(Modifier.isStatic(m) && Modifier.isFinal(m) && Modifier.isPublic(m), "LAME_MAX doit être public static final.");
            Check.equal(120, f.getInt(null), "LAME_MAX");
        });
        Check.test("Le compteur est un champ static non public", () -> {
            int n = 0;
            for (Field f : Check.playerClass("Epee").getDeclaredFields()) {
                int m = f.getModifiers();
                if (Modifier.isStatic(m) && !Modifier.isFinal(m)) {
                    n++;
                    Check.isTrue(Modifier.isPrivate(m), "Le compteur doit être private static.");
                }
            }
            Check.equal(1, n, "Nombre de champs static modifiables (le compteur)");
        });
        Check.test("getForgees est static", () -> {
            Method m = Check.playerClass("Epee").getDeclaredMethod("getForgees");
            Check.isTrue(Modifier.isStatic(m.getModifiers()), "getForgees() doit être static.");
        });
        Check.test("Le compteur augmente d'un par épée", () -> {
            int avant = Epee.getForgees();
            new Epee("Aube");
            Check.equal(avant + 1, Epee.getForgees(), "Après une épée");
            new Epee("Crépuscule");
            new Epee("Aurore");
            Check.equal(avant + 3, Epee.getForgees(), "Après trois épées");
        });
        Check.test("Le compteur est partagé et non remis à zéro", () -> {
            Epee a = new Epee("A");
            int apresA = Epee.getForgees();
            Epee b = new Epee("B");
            Check.equal(apresA + 1, Epee.getForgees(), "Après la deuxième épée");
            Check.equal("A", a.getNom(), "Nom de la première épée");
            Check.equal("B", b.getNom(), "Nom de la seconde épée");
        });
    }
}
