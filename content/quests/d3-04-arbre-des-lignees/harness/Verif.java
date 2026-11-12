import fr.javaquest.runner.Check;
import java.lang.reflect.*;
import java.util.Arrays;

class Verif {
    static void refuse(Runnable r, String quand) {
        Throwable t = null;
        try { r.run(); } catch (Throwable e) { t = e; }
        if (t == null) Check.fail(quand + " : aucune exception levée (attendu IllegalArgumentException).");
        Check.isTrue(t instanceof IllegalArgumentException, quand + " : attendu IllegalArgumentException, obtenu " + t.getClass().getSimpleName() + ".");
    }
    static boolean declare(Class<?> c, String nom) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(nom)) return true;
        return false;
    }
    static String noms(Noble[] t) {
        StringBuilder sb = new StringBuilder();
        for (Noble n : t) sb.append(n.nom()).append(' ');
        return sb.toString().trim();
    }
    static void verifier() {
        Check.test("Structure : classe abstraite, Comparable, deux sous-classes", () -> {
            Class<?> n = Check.playerClass("Noble"), b = Check.playerClass("Baron"), c = Check.playerClass("Comte");
            Check.isTrue(Modifier.isAbstract(n.getModifiers()) && !n.isInterface(), "Noble doit être une classe abstraite.");
            Check.isTrue(Comparable.class.isAssignableFrom(n), "Noble doit implémenter Comparable<Noble>.");
            Check.isTrue(Modifier.isAbstract(n.getMethod("tribut").getModifiers()), "tribut() doit être abstraite dans Noble.");
            Check.isTrue(Modifier.isAbstract(n.getMethod("titre").getModifiers()), "titre() doit être abstraite dans Noble.");
            Check.isTrue(b.getSuperclass() == n && c.getSuperclass() == n, "Baron et Comte doivent étendre Noble.");
            Check.isTrue(!Modifier.isAbstract(b.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "Baron et Comte doivent être concrètes.");
            Check.isTrue(declare(n, "compareTo") && !declare(b, "compareTo") && !declare(c, "compareTo"), "compareTo s'écrit une seule fois, dans Noble.");
        });
        Check.test("Baron : titre, tribut (fortune / 10), toString", () -> {
            Baron b = new Baron("Aldric", 1000);
            Check.equal("Baron", b.titre(), "titre()");
            Check.equal("Aldric", b.nom(), "nom()");
            Check.equal(1000, b.fortune(), "fortune()");
            Check.equal(100, b.tribut(), "tribut() de 1000");
            Check.equal(0, new Baron("Petit", 9).tribut(), "tribut() de 9 (division entière)");
            Check.equal(0, new Baron("Gueux", 0).tribut(), "tribut() de 0");
            Check.equal("Baron Aldric (100)", b.toString(), "toString()");
        });
        Check.test("Comte : titre, tribut (fortune / 5 + 50), toString", () -> {
            Comte c = new Comte("Berte", 1000);
            Check.equal("Comte", c.titre(), "titre()");
            Check.equal(250, c.tribut(), "tribut() de 1000");
            Check.equal(50, new Comte("Fauché", 0).tribut(), "tribut() de 0");
            Check.equal(50, new Comte("Presque", 4).tribut(), "tribut() de 4 (division entière)");
            Check.equal("Comte Berte (250)", c.toString(), "toString()");
        });
        Check.test("Validation du constructeur (héritée de Noble)", () -> {
            refuse(() -> new Baron(null, 10), "nom null");
            refuse(() -> new Comte("  ", 10), "nom blanc");
            refuse(() -> new Baron("Aldric", -1), "fortune négative");
            Check.equal("Aldric", new Comte("  Aldric ", 10).nom(), "nom() après espaces autour du nom");
            Check.equal(0, new Baron("Zéro", 0).fortune(), "fortune 0 acceptée");
        });
        Check.test("compareTo : plus gros tribut d'abord, puis ordre alphabétique", () -> {
            Noble comte = new Comte("Berte", 1000), baron = new Baron("Aldric", 1000);
            Check.isTrue(comte.compareTo(baron) < 0, "Comte (250) avant Baron (100).");
            Check.isTrue(baron.compareTo(comte) > 0, "Baron (100) après Comte (250).");
            Noble zed = new Baron("Zed", 500), amy = new Comte("Amy", 0);
            Check.isTrue(amy.compareTo(zed) < 0, "Tribut égal (50) : Amy avant Zed.");
            Check.isTrue(zed.compareTo(amy) > 0, "Tribut égal (50) : Zed après Amy.");
            Check.equal(0, new Baron("Aldric", 1000).compareTo(new Baron("Aldric", 1005)), "Même titre, même nom, même tribut");
            Check.isTrue(new Baron("A", Integer.MAX_VALUE).compareTo(new Comte("B", 0)) < 0, "Fortune extrême : pas de débordement.");
        });
        Check.test("Cour.classer : tri polymorphe d'un tableau, sans toucher l'original", () -> {
            Noble[] t = { new Baron("Zed", 500), new Baron("Aldric", 1000), new Comte("Amy", 0), new Comte("Berte", 1000), new Baron("Bob", 900) };
            Noble[] r = Cour.classer(t);
            Check.equal("Berte Aldric Bob Amy Zed", noms(r), "Ordre du tableau classé (Berte 250, Aldric 100, Bob 90, Amy et Zed 50)");
            Check.equal("Zed Aldric Amy Berte Bob", noms(t), "Le tableau d'origine ne doit pas être modifié");
            Check.isTrue(r != t, "classer doit renvoyer un nouveau tableau.");
            Check.equal(0, Cour.classer(new Noble[0]).length, "Tableau vide");
            Noble seul = new Comte("Seul", 7);
            Check.isTrue(Cour.classer(new Noble[] { seul })[0] == seul, "Un seul noble");
        });
    }
}
