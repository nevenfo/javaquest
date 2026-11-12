
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Accesseurs et toString", () -> {
            Grimoire g = new Grimoire("Ombres", 120);
            Check.equal("Ombres", g.getTitre(), "getTitre()");
            Check.equal(120, g.getPages(), "getPages()");
            Check.equal("Ombres/120", g.toString(), "toString()");
        });
        Check.test("Tri par pages puis titre", () -> {
            List<Grimoire> l = new ArrayList<>(List.of(new Grimoire("Z", 50), new Grimoire("B", 300), new Grimoire("A", 50), new Grimoire("M", 5)));
            Collections.sort(l);
            Check.equal("[M/5, A/50, Z/50, B/300]", l.toString(), "Liste triée");
        });
        Check.test("compareTo : signes, égalité, extrêmes", () -> {
            Check.isTrue(new Grimoire("A", 1).compareTo(new Grimoire("A", 2)) < 0, "Moins de pages : avant.");
            Check.isTrue(new Grimoire("A", 2).compareTo(new Grimoire("A", 1)) > 0, "Plus de pages : après.");
            Check.equal(0, new Grimoire("A", 2).compareTo(new Grimoire("A", 2)), "Identiques");
            Check.isTrue(new Grimoire("A", 2_000_000_000).compareTo(new Grimoire("A", -2_000_000_000)) > 0, "Pas de débordement.");
        });
    }
}
