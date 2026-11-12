import fr.javaquest.runner.Check;
import java.util.Arrays;

class Verif {
    static Heritier h(String nom, int annee) { return new Heritier(nom, annee, "Courage"); }

    static void verifier() {
        Check.test("L'héritier proclame sa propre devise (via une variable Ancetre)", () -> {
            Ancetre a = new Heritier("Aldo", 1200, "Courage");
            Check.equal("Honneur et Courage", a.devise(), "devise() appelée sur une variable de type Ancetre");
            Check.equal("Aldo (1200) : Honneur et Courage", a.presenter(), "presenter()");
            Check.equal("Honneur et Clarté", new Heritier("Maëlle", 1350, "Clarté").devise(), "devise() d'un autre héritier");
            Check.equal("Brann (1100) : Honneur", new Ancetre("Brann", 1100).presenter(), "Un Ancetre simple garde la devise d'origine");
        });
        Check.test("Ordre : le plus ancien d'abord", () -> {
            Check.isTrue(h("A", 1100).compareTo(h("B", 1200)) < 0, "1100 doit passer avant 1200.");
            Check.isTrue(h("A", 1200).compareTo(h("B", 1100)) > 0, "1200 doit passer après 1100.");
            Check.isTrue(h("A", -50).compareTo(h("B", 0)) < 0, "Une année négative passe avant 0.");
            Check.equal(0, h("A", 1200).compareTo(h("A", 1200)), "Même année, même nom");
        });
        Check.test("Même année : ordre alphabétique des noms", () -> {
            Check.isTrue(h("Aldo", 1200).compareTo(h("Berte", 1200)) < 0, "Aldo avant Berte.");
            Check.isTrue(h("Berte", 1200).compareTo(h("Aldo", 1200)) > 0, "Berte après Aldo.");
        });
        Check.test("Années extrêmes : pas de débordement", () -> {
            Check.isTrue(h("A", Integer.MIN_VALUE).compareTo(h("B", 5)) < 0, "MIN_VALUE avant 5.");
            Check.isTrue(h("A", Integer.MAX_VALUE).compareTo(h("B", -5)) > 0, "MAX_VALUE après -5.");
            Check.isTrue(h("A", Integer.MIN_VALUE).compareTo(h("B", Integer.MAX_VALUE)) < 0, "MIN_VALUE avant MAX_VALUE.");
        });
        Check.test("Tri d'un tableau avec Arrays.sort", () -> {
            Heritier[] t = { h("Zoé", 1300), h("Berte", 1200), h("Aldo", 1200), h("Yvon", 900), h("Cyril", 1500) };
            Arrays.sort(t);
            StringBuilder sb = new StringBuilder();
            for (Heritier x : t) sb.append(x.presenter().split(" ")[0]).append(' ');
            Check.equal("Yvon Aldo Berte Zoé Cyril", sb.toString().trim(), "Ordre après Arrays.sort");
        });
    }
}
