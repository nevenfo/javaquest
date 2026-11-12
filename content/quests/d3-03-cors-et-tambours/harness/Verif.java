import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static Signal anonyme(String n, int p) {
        return new Signal() {
            public String nom() { return n; }
            public int portee() { return p; }
        };
    }
    static boolean declare(Class<?> c, String nom) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(nom)) return true;
        return false;
    }
    static void verifier() {
        Check.test("Structure : interface, default, static, deux implémentations", () -> {
            Class<?> s = Check.playerClass("Signal");
            Check.isTrue(s.isInterface(), "Signal doit être une interface.");
            Check.isTrue(s.getMethod("annonce").isDefault(), "annonce() doit être une méthode default de Signal.");
            Check.isTrue(Modifier.isStatic(s.getMethod("plusLointain", Signal[].class).getModifiers()), "plusLointain doit être static.");
            Check.isTrue(s.isAssignableFrom(Check.playerClass("Cor")), "Cor doit implémenter Signal.");
            Check.isTrue(s.isAssignableFrom(Check.playerClass("Tambour")), "Tambour doit implémenter Signal.");
            Check.isTrue(!declare(Check.playerClass("Cor"), "annonce"), "Cor ne doit pas réécrire annonce() : il profite de la version default.");
            Check.isTrue(declare(Check.playerClass("Tambour"), "annonce"), "Tambour doit redéfinir annonce().");
        });
        Check.test("Cor : nom, portée, annonce par défaut", () -> {
            Signal c = new Cor(5);
            Check.equal("cor", c.nom(), "nom()");
            Check.equal(50, c.portee(), "portee() de Cor(5)");
            Check.equal(0, new Cor(0).portee(), "portee() de Cor(0)");
            Check.equal("cor : 50 pas", c.annonce(), "annonce() de Cor(5)");
            Check.equal("cor : 0 pas", new Cor(0).annonce(), "annonce() de Cor(0)");
        });
        Check.test("Tambour : portée et annonce redéfinie (via une variable Signal)", () -> {
            Signal t = new Tambour(10);
            Check.equal("tambour", t.nom(), "nom()");
            Check.equal(40, t.portee(), "portee() de Tambour(10)");
            Check.equal("BOUM ! tambour : 40 pas", t.annonce(), "annonce() de Tambour(10) appelée sur un Signal");
            Check.equal("BOUM ! tambour : 4 pas", new Tambour(1).annonce(), "annonce() de Tambour(1)");
        });
        Check.test("La version default s'appuie sur nom() et portee() de l'objet", () -> {
            Check.equal("sirène : 120 pas", anonyme("sirène", 120).annonce(), "annonce() d'un signal inconnu");
            Check.equal("fumée : -3 pas", anonyme("fumée", -3).annonce(), "annonce() avec une portée négative");
        });
        Check.test("Signal.plusLointain : traitement polymorphe", () -> {
            Signal cor = new Cor(5), tam = new Tambour(20), sir = anonyme("sirène", 70);
            Check.isTrue(Signal.plusLointain(new Signal[] { cor, tam, sir }) == tam, "Tambour(20) porte à 80 pas : le plus loin des trois.");
            Check.isTrue(Signal.plusLointain(new Signal[] { cor, new Tambour(3) }) == cor, "Cor(5) (50) devance Tambour(3) (12).");
            Check.isTrue(Signal.plusLointain(new Signal[] { sir }) == sir, "Un seul signal : c'est lui.");
            Check.isTrue(Signal.plusLointain(new Signal[0]) == null, "Aucun signal : null.");
            Signal a = new Cor(4), b = new Tambour(10);
            Check.isTrue(Signal.plusLointain(new Signal[] { a, b }) == a, "À portée égale (40), le premier du tableau gagne.");
        });
    }
}
