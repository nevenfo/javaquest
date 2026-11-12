
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<String> l(String... t) { return Arrays.asList(t); }
    static void verifier() {
        Check.test("premierDoublon : cas simple", () -> {
            Check.equal("b", Main.premierDoublon(l("a", "b", "c", "b", "a")), "premierDoublon([a, b, c, b, a])");
            Check.equal("zz", Main.premierDoublon(l("x", "zz", "y", "zz")), "premierDoublon([x, zz, y, zz])");
        });
        Check.test("premierDoublon : doublon construit à part", () ->
            Check.equal("ab", Main.premierDoublon(l("ab", "cd", new String("ab"))), "Deux chaînes égales créées séparément"));
        Check.test("premierDoublon : aucun doublon, liste vide, un élément", () -> {
            Check.isTrue(Main.premierDoublon(l("a", "b", "c")) == null, "Pas de doublon : null attendu.");
            Check.isTrue(Main.premierDoublon(l()) == null, "Liste vide : null attendu.");
            Check.isTrue(Main.premierDoublon(l("a")) == null, "Un seul élément : null attendu.");
        });
        Check.test("sansDoublons garde l'ordre de première apparition", () -> {
            Check.equal(List.of("zèbre", "ancre", "mage", "orbe", "dragon"), Main.sansDoublons(l("zèbre", "ancre", "zèbre", "mage", "ancre", "orbe", "dragon", "mage")), "sansDoublons");
        });
        Check.test("sansDoublons : limites", () -> {
            Check.equal(List.of(), Main.sansDoublons(l()), "Liste vide");
            Check.equal(List.of("a"), Main.sansDoublons(l("a", "a", "a")), "Un seul mot répété");
        });
        Check.test("alphabetique", () -> {
            Check.equal(List.of("ancre", "dragon", "mage", "orbe", "zèbre"), Main.alphabetique(l("zèbre", "ancre", "zèbre", "mage", "dragon", "orbe", "mage")), "alphabetique");
            Check.equal(List.of(), Main.alphabetique(l()), "Liste vide");
            Check.equal(List.of("B", "a"), Main.alphabetique(l("a", "B", "a")), "Majuscules avant minuscules (ordre naturel)");
        });
        Check.test("Les listes reçues ne sont pas modifiées", () -> {
            List<String> src = new ArrayList<>(l("b", "a", "b"));
            Main.sansDoublons(src); Main.alphabetique(src); Main.premierDoublon(src);
            Check.equal(List.of("b", "a", "b"), src, "Liste d'origine");
        });
    }
}
