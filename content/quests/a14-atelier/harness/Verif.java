
import fr.javaquest.runner.Check;
import java.lang.reflect.*;
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
        Check.test("Armure : accesseurs, toString", () -> {
            Armure a = new Armure("Plastron", 3);
            Check.equal("Plastron", a.getNom(), "getNom()");
            Check.equal(3, a.getNiveau(), "getNiveau()");
            Check.equal("Armure[Plastron, niveau 3]", a.toString(), "toString()");
        });
        Check.test("Armure : validation", () -> {
            refuse(() -> new Armure(null, 1), "nom null");
            refuse(() -> new Armure("  ", 1), "nom blanc");
            refuse(() -> new Armure("Casque", 0), "niveau 0");
            refuse(() -> new Armure("Casque", 11), "niveau 11");
            new Armure("Casque", 1); new Armure("Casque", 10);
        });
        Check.test("Armure : equals / hashCode", () -> {
            Object a = new Armure(new String("Heaume"), 4), b = new Armure(new String("Heaume"), 4);
            Check.isTrue(a.equals(b), "Deux armures de même nom et niveau doivent être égales.");
            Check.isTrue(!a.equals(new Armure("Heaume", 5)), "Niveaux différents : armures différentes.");
            Check.isTrue(!a.equals(new Armure("Gantelet", 4)), "Noms différents : armures différentes.");
            Check.isTrue(!a.equals(null) && !a.equals("Heaume"), "equals(null) / autre type : false.");
            Check.equal(a.hashCode(), b.hashCode(), "hashCode d'armures égales");
        });
        Check.test("Atelier : refuse les doublons", () -> {
            Atelier at = new Atelier();
            Check.isTrue(at.commander(new Armure("Plastron", 3)), "Première commande : true.");
            Check.isTrue(at.commander(new Armure("Heaume", 3)), "Autre armure : true.");
            Check.isTrue(!at.commander(new Armure(new String("Plastron"), 3)), "Doublon : commander doit renvoyer false.");
            Check.equal(2, at.total(), "total()");
        });
        Check.test("Atelier vide et null", () -> {
            Atelier at = new Atelier();
            Check.equal(0, at.total(), "total() d'un atelier vide");
            Check.equal(List.of(), at.commandes(), "commandes() d'un atelier vide");
            refuse(() -> at.commander(null), "commander(null)");
        });
        Check.test("Atelier : ordre de commande", () -> {
            Atelier at = new Atelier();
            at.commander(new Armure("C", 1)); at.commander(new Armure("A", 2)); at.commander(new Armure("B", 3)); at.commander(new Armure("A", 2));
            Check.equal("[Armure[C, niveau 1], Armure[A, niveau 2], Armure[B, niveau 3]]", at.commandes().toString(), "commandes()");
        });
        Check.test("Atelier : commandes() renvoie une copie", () -> {
            Atelier at = new Atelier();
            at.commander(new Armure("A", 1));
            try { at.commandes().clear(); } catch (UnsupportedOperationException e) { /* copie non modifiable : accepté */ }
            Check.equal(1, at.total(), "total() après modification de la liste reçue");
            Check.equal(1, at.commandes().size(), "commandes() après modification de la liste reçue");
        });
        Check.test("Encapsulation des champs", () -> {
            for (String n : new String[]{"Armure", "Atelier"})
                for (Field f : Check.playerClass(n).getDeclaredFields())
                    if (!Modifier.isStatic(f.getModifiers()))
                        Check.isTrue(Modifier.isPrivate(f.getModifiers()), n + " : le champ " + f.getName() + " doit être private.");
        });
    }
}
