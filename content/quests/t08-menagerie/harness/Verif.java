
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static Set<Class<?>> capacites(Class<?> c) {
        Set<Class<?>> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) if (i == Marcheur.class || i == Nageur.class || i == Volant.class) s.add(i);
        return s;
    }
    static void verifier() {
        Check.test("Animal : classe abstraite sans capacité", () -> {
            Class<?> a = Check.playerClass("Animal");
            Check.isTrue(java.lang.reflect.Modifier.isAbstract(a.getModifiers()), "Animal doit être abstract.");
            Check.isTrue(capacites(a).isEmpty(), "Animal ne doit implémenter aucune capacité : tous les animaux ne marchent pas, ne nagent pas, ne volent pas.");
        });
        Check.test("Canard : Animal + 3 capacités", () -> {
            Class<?> c = Check.playerClass("Canard");
            Check.equal(Check.playerClass("Animal"), c.getSuperclass(), "Parent de Canard");
            Check.equal(Set.of(Marcheur.class, Nageur.class, Volant.class), capacites(c), "Capacités de Canard");
        });
        Check.test("Grenouille : marche et nage", () -> {
            Class<?> c = Check.playerClass("Grenouille");
            Check.equal(Check.playerClass("Animal"), c.getSuperclass(), "Parent de Grenouille");
            Check.equal(Set.of(Marcheur.class, Nageur.class), capacites(c), "Capacités de Grenouille");
        });
        Check.test("Poisson : nage seulement", () -> {
            Class<?> c = Check.playerClass("Poisson");
            Check.equal(Check.playerClass("Animal"), c.getSuperclass(), "Parent de Poisson");
            Check.equal(Set.of(Nageur.class), capacites(c), "Capacités de Poisson");
        });
        Check.test("Textes des capacités et getNom", () -> {
            Canard d = new Canard("Daisy");
            Check.equal("Daisy", d.getNom(), "getNom()");
            Check.equal("Daisy marche", d.marcher(), "marcher()");
            Check.equal("Daisy nage", d.nager(), "nager()");
            Check.equal("Daisy vole", d.voler(), "voler()");
            Check.equal("Rita nage", new Grenouille("Rita").nager(), "Grenouille.nager()");
            Check.equal("Rita marche", new Grenouille("Rita").marcher(), "Grenouille.marcher()");
            Check.equal("Bulle nage", new Poisson("Bulle").nager(), "Poisson.nager()");
        });
        Check.test("Main : la scène de la ménagerie", () -> {
            String out = Check.runMain("Main", "");
            Check.equal(List.of("Donald marche", "Donald nage", "Donald vole",
                    "Kermit marche", "Kermit nage", "Kermit ne sait pas voler",
                    "Nemo ne sait pas marcher", "Nemo nage", "Nemo ne sait pas voler"), Check.lines(out), "Sortie de Main");
        });
    }
}
