
import fr.javaquest.runner.Check;
import java.util.*;

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
        Check.test("Lot est un record", () -> {
            Check.isTrue(Check.playerClass("Lot").isRecord(), "Lot doit rester un record.");
            Check.equal("Lot[metal=or, quantite=4]", new Lot("or", 4).toString(), "toString()");
        });
        Check.test("Lots valides (dont les limites)", () -> {
            Check.equal(0, new Lot("fer", 0).quantite(), "Quantité 0 autorisée");
            Check.equal("a", new Lot("a", 1).metal(), "Métal d'un caractère");
        });
        Check.test("Quantité négative refusée", () -> refuse(() -> new Lot("or", -1), "new Lot(\"or\", -1)"));
        Check.test("Métal null refusé", () -> refuse(() -> new Lot(null, 3), "new Lot(null, 3)"));
        Check.test("Métal vide ou blanc refusé", () -> {
            refuse(() -> new Lot("", 3), "new Lot(\"\", 3)");
            refuse(() -> new Lot("   ", 3), "new Lot(\"   \", 3)");
        });
        Check.test("ajouter", () -> {
            Lot a = new Lot("cuivre", 5);
            Lot b = a.ajouter(3);
            Check.equal(new Lot("cuivre", 8), b, "ajouter(3)");
            Check.equal(5, a.quantite(), "Le lot d'origine est inchangé");
            Check.equal(new Lot("cuivre", 0), a.ajouter(-5), "ajouter(-5) : limite 0");
            refuse(() -> a.ajouter(-6), "ajouter(-6) sur 5 unités");
        });
        Check.test("estVide et vide", () -> {
            Check.isTrue(new Lot("or", 0).estVide(), "Un lot de 0 est vide.");
            Check.isTrue(!new Lot("or", 1).estVide(), "Un lot de 1 n'est pas vide.");
            Check.equal(new Lot("zinc", 0), Lot.vide("zinc"), "Lot.vide(\"zinc\")");
        });
        Check.test("Égalité et HashSet", () -> {
            Set<Lot> s = new HashSet<>();
            s.add(new Lot("or", 2)); s.add(new Lot("or", 2)); s.add(new Lot("or", 3));
            Check.equal(2, s.size(), "Lots distincts");
        });
    }
}
