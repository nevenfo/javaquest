import fr.javaquest.runner.Check;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

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
        Check.test("C'est un record (modele, taille)", () -> {
            Class<?> c = Check.playerClass("Gabarit");
            Check.isTrue(c.isRecord(), "Gabarit doit être un record.");
            var comp = c.getRecordComponents();
            Check.equal(2, comp.length, "Nombre de composants");
            Check.equal("modele", comp[0].getName(), "Premier composant");
            Check.equal(String.class, comp[0].getType(), "Type du premier composant");
            Check.equal("taille", comp[1].getName(), "Second composant");
            Check.equal(int.class, comp[1].getType(), "Type du second composant");
        });
        Check.test("Accesseurs, toString et nettoyage du modèle", () -> {
            Gabarit g = new Gabarit("cloche", 3);
            Check.equal("cloche", g.modele(), "modele()");
            Check.equal(3, g.taille(), "taille()");
            Check.equal("Gabarit[modele=cloche, taille=3]", g.toString(), "toString()");
            Check.equal("cloche", new Gabarit("  cloche \t", 3).modele(), "modele() après espaces autour du nom");
        });
        Check.test("Modèle invalide", () -> {
            refuse(() -> new Gabarit(null, 3), "modèle null");
            refuse(() -> new Gabarit("", 3), "modèle vide");
            refuse(() -> new Gabarit("   ", 3), "modèle blanc");
        });
        Check.test("Taille invalide (bornes 1 à 20 incluses)", () -> {
            refuse(() -> new Gabarit("a", 0), "taille 0");
            refuse(() -> new Gabarit("a", -4), "taille -4");
            refuse(() -> new Gabarit("a", 21), "taille 21");
            Check.equal(1, new Gabarit("a", 1).taille(), "taille 1 acceptée");
            Check.equal(20, new Gabarit("a", 20).taille(), "taille 20 acceptée");
        });
        Check.test("equals / hashCode : modèle nettoyé, HashSet", () -> {
            Object a = new Gabarit(new String("cloche"), 3), b = new Gabarit("  cloche ", 3);
            Check.isTrue(a.equals(b), "« cloche » et « cloche » entouré d'espaces, même taille : gabarits égaux.");
            Check.equal(a.hashCode(), b.hashCode(), "hashCode de gabarits égaux");
            Check.isTrue(!a.equals(new Gabarit("cloche", 4)), "Tailles différentes : gabarits différents.");
            Check.isTrue(!a.equals(new Gabarit("lingot", 3)), "Modèles différents : gabarits différents.");
            Check.isTrue(!a.equals(null), "equals(null) : false.");
            Set<Gabarit> s = new HashSet<>();
            s.add(new Gabarit("cloche", 3)); s.add(new Gabarit(" cloche", 3)); s.add(new Gabarit("cloche", 5));
            Check.equal(2, s.size(), "Taille du HashSet");
        });
        Check.test("volume()", () -> {
            Check.equal(27, new Gabarit("a", 3).volume(), "volume() de taille 3");
            Check.equal(1, new Gabarit("a", 1).volume(), "volume() de taille 1");
            Check.equal(8000, new Gabarit("a", 20).volume(), "volume() de taille 20");
        });
        Check.test("agrandir(delta) : nouveau gabarit, mêmes règles", () -> {
            Gabarit g = new Gabarit("cloche", 3);
            Check.equal(new Gabarit("cloche", 7), g.agrandir(4), "agrandir(4)");
            Check.equal(new Gabarit("cloche", 1), g.agrandir(-2), "agrandir(-2)");
            Check.equal(3, g.taille(), "L'original ne change pas (record immuable)");
            refuse(() -> g.agrandir(18), "agrandir au-delà de 20");
            refuse(() -> g.agrandir(-3), "agrandir sous 1");
        });
        Check.test("Gabarit.unitaire(modele) : méthode static", () -> {
            Method m = null;
            for (Method x : Check.playerClass("Gabarit").getDeclaredMethods())
                if (x.getName().equals("unitaire") && x.getParameterCount() == 1) m = x;
            Check.isTrue(m != null, "La méthode unitaire(String) est introuvable.");
            Check.isTrue(Modifier.isStatic(m.getModifiers()), "unitaire doit être static.");
            Check.equal(new Gabarit("moule", 1), m.invoke(null, "moule"), "unitaire(\"moule\")");
            Check.equal(new Gabarit("moule", 1), m.invoke(null, " moule "), "unitaire(\" moule \")");
        });
    }
}
