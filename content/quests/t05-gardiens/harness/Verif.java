
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static void verifier() {
        Check.test("Veilleur et Golem héritent de Gardien", () -> {
            Check.equal(Gardien.class, Check.playerClass("Veilleur").getSuperclass(), "Parent de Veilleur");
            Check.equal(Gardien.class, Check.playerClass("Golem").getSuperclass(), "Parent de Golem");
        });
        Check.test("Pas de champ nom redéclaré", () -> {
            for (String n : new String[]{"Veilleur", "Golem"})
                for (Field f : Check.playerClass(n).getDeclaredFields())
                    Check.isTrue(!f.getName().equals("nom"), n + " ne doit pas redéclarer le champ nom : il est hérité.");
        });
        Check.test("Golem réutilise presentation() héritée", () -> {
            for (Method m : Check.playerClass("Golem").getDeclaredMethods())
                Check.isTrue(!m.getName().equals("presentation"), "Golem n'a pas besoin de redéfinir presentation().");
            Check.equal("[Rocky | force 20]", new Golem("Rocky").presentation(), "presentation() du golem");
            Check.equal(20, new Golem("Rocky").force(), "force() du golem");
        });
        int[] vig = {4, 0, 11};
        for (int v : vig)
            Check.test("Veilleur de vigilance " + v, () -> {
                Gardien g = new Veilleur("Aldo", v);
                Check.equal(2 * v, g.force(), "force()");
                Check.equal("[Aldo | force " + (2 * v) + "] veille", g.presentation(), "presentation()");
            });
        Check.test("Appels polymorphes via Gardien", () -> {
            Gardien[] t = {new Golem("G"), new Veilleur("V", 1)};
            Check.equal("[G | force 20]", t[0].presentation(), "t[0].presentation()");
            Check.equal("[V | force 2] veille", t[1].presentation(), "t[1].presentation()");
        });
    }
}
