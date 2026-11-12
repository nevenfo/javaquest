
import fr.javaquest.runner.Check;
import java.lang.reflect.Modifier;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Structure : classe abstraite, héritage, interface default", () -> {
            Check.isTrue(Modifier.isAbstract(Creature.class.getModifiers()), "Creature doit être abstraite.");
            Check.isTrue(Loup.class.getSuperclass() == Creature.class, "Loup doit étendre Creature.");
            Check.isTrue(Dragon.class.getSuperclass() == Creature.class, "Dragon doit étendre Creature.");
            Check.isTrue(Volant.class.isInterface(), "Volant doit être une interface.");
            Check.isTrue(Volant.class.getMethod("envol").isDefault(), "envol() doit être une méthode default.");
            Check.isTrue(Volant.class.isAssignableFrom(Dragon.class), "Dragon doit implémenter Volant.");
        });
        Check.test("Loup", () -> {
            Creature c = new Loup("Croc");
            Check.equal(5, c.force(), "force d'un loup");
            Check.equal("Croc (force 5)", c.presenter(), "presenter() d'un loup");
            Check.equal("Croc", c.nom(), "nom()");
        });
        Check.test("Dragon : super et default", () -> {
            Dragon d = new Dragon("Smaug", 3);
            Check.equal(16, d.force(), "force d'un dragon à 3 écailles");
            Check.equal("Smaug (force 16) crache du feu", d.presenter(), "presenter() d'un dragon");
            Check.equal("bat des ailes", d.envol(), "envol() fourni par l'interface");
            Check.equal("Ig (force 10) crache du feu", new Dragon("Ig", 0).presenter(), "dragon sans écaille");
            Check.isTrue(!Arrays.stream(Dragon.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals("envol")), "Dragon ne doit pas recopier envol().");
        });
        Check.test("Main.forceTotale", () -> {
            Check.equal(0, Main.forceTotale(new ArrayList<>()), "forceTotale([])");
            List<Creature> l = List.of(new Loup("A"), new Dragon("B", 0), new Dragon("C", 3));
            Check.equal(31, Main.forceTotale(l), "forceTotale([loup, dragon 10, dragon 16])");
            Check.equal(5, Main.forceTotale(List.of(new Loup("Seul"))), "un seul loup");
        });
        Check.test("Main.rapport", () -> {
            List<Creature> l = List.of(new Dragon("B", 1), new Loup("A"));
            Check.equal(List.of("B (force 12) crache du feu", "A (force 5)"), Main.rapport(l), "rapport");
            Check.equal(List.of(), Main.rapport(new ArrayList<>()), "rapport([])");
        });
    }
}
