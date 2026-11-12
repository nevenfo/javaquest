
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<String> l(String... t) { return Arrays.asList(t); }
    static void verifier() {
        Check.test("compter : cas simple", () -> {
            Check.equal(Map.of("or", 2, "fer", 1), Main.compter(l("or", "fer", "or")), "compter([or, fer, or])");
        });
        Check.test("compter : un mot répété, un mot unique, liste vide", () -> {
            Check.equal(Map.of("a", 4), Main.compter(l("a", "a", "a", "a")), "compter([a, a, a, a])");
            Check.equal(Map.of("seul", 1), Main.compter(l("seul")), "compter([seul])");
            Check.equal(Map.of(), Main.compter(l()), "compter([])");
        });
        Check.test("compter : mots égaux construits séparément et casse", () -> {
            Check.equal(Map.of("x", 2, "X", 1), Main.compter(l("x", new String("x"), "X")), "x, x (autre objet), X");
        });
        Check.test("plusFrequent", () -> {
            Check.equal("a", Main.plusFrequent(l("b", "a", "b", "a", "c")), "plusFrequent([b, a, b, a, c]) : égalité, ordre alphabétique");
            Check.equal("rune", Main.plusFrequent(l("or", "rune", "fer", "rune", "or", "rune")), "plusFrequent avec un vainqueur net");
        });
        Check.test("plusFrequent : égalités sur plusieurs mots", () -> {
            Check.equal("ancre", Main.plusFrequent(l("zèbre", "orbe", "ancre", "mage", "ancre", "zèbre", "orbe")), "Trois mots à 2 occurrences");
            Check.equal("m", Main.plusFrequent(l("z", "m")), "Tous à 1 occurrence");
        });
        Check.test("plusFrequent : limites", () -> {
            Check.isTrue(Main.plusFrequent(l()) == null, "Liste vide : null attendu.");
            Check.equal("seul", Main.plusFrequent(l("seul")), "Un seul mot");
        });
    }
}
