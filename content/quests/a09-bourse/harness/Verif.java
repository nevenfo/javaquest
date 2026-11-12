
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    /** Exécute r et renvoie la classe de l'exception levée (null si aucune). */
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
        Check.test("Usage normal", () -> {
            Bourse b = new Bourse(10);
            b.deposer(5);
            Check.equal(15, b.getSolde(), "Solde après dépôt");
            b.retirer(15);
            Check.equal(0, b.getSolde(), "Solde après retrait total (limite)");
            Check.equal(0, new Bourse(0).getSolde(), "Bourse vide autorisée");
        });
        Check.test("Solde initial négatif", () ->
            attendre(IllegalArgumentException.class, () -> new Bourse(-1), "new Bourse(-1)"));
        Check.test("Dépôt invalide", () -> {
            Bourse b = new Bourse(10);
            attendre(IllegalArgumentException.class, () -> b.deposer(0), "deposer(0)");
            attendre(IllegalArgumentException.class, () -> b.deposer(-4), "deposer(-4)");
            Check.equal(10, b.getSolde(), "Solde après dépôts refusés");
        });
        Check.test("Retrait invalide : argument", () -> {
            Bourse b = new Bourse(10);
            attendre(IllegalArgumentException.class, () -> b.retirer(0), "retirer(0)");
            attendre(IllegalArgumentException.class, () -> b.retirer(-3), "retirer(-3)");
        });
        Check.test("Retrait trop gros : état", () -> {
            Bourse b = new Bourse(10);
            attendre(IllegalStateException.class, () -> b.retirer(11), "retirer(11) avec 10 pièces");
            Check.equal(10, b.getSolde(), "Le solde ne doit pas changer après un retrait refusé");
        });
        Check.test("Champs privés", () -> {
            for (Field f : Check.playerClass("Bourse").getDeclaredFields())
                if (!Modifier.isStatic(f.getModifiers()))
                    Check.isTrue(Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
        });
    }
}
