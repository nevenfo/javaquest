
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<String> l(String... t) { return new ArrayList<>(Arrays.asList(t)); }
    static void verifier() {
        Check.test("sansDoublons : ordre de première apparition", () -> {
            Check.equal(List.of("or", "fer", "cuivre"), Main.sansDoublons(l("or", "fer", "or", "cuivre", "fer")), "sansDoublons");
            Check.equal(List.of("z", "a", "m"), Main.sansDoublons(l("z", "a", "z", "m", "a")), "sans tri alphabétique");
        });
        Check.test("sansDoublons : limites", () -> {
            Check.equal(List.of(), Main.sansDoublons(l()), "liste vide");
            Check.equal(List.of("x"), Main.sansDoublons(l("x", "x", "x")), "un seul objet répété");
            List<String> original = l("a", "a", "b");
            Main.sansDoublons(original);
            Check.equal(List.of("a", "a", "b"), original, "la liste d'origine ne doit pas être modifiée");
        });
        Check.test("positions", () -> {
            Check.equal(Map.of("or", List.of(0, 2), "fer", List.of(1)), Main.positions(l("or", "fer", "or")), "positions([or, fer, or])");
            Check.equal(Map.of(), Main.positions(l()), "positions([])");
            Check.equal(Map.of("a", List.of(0, 1, 3), "b", List.of(2)), Main.positions(l("a", "a", "b", "a")), "positions([a, a, b, a])");
        });
        Check.test("retirerTous : occurrences consécutives", () -> {
            List<String> x = l("a", "a", "b", "a");
            Main.retirerTous(x, "a");
            Check.equal(List.of("b"), x, "retirerTous([a, a, b, a], a)");
        });
        Check.test("retirerTous : autres cas", () -> {
            List<String> x = l("a", "b", "c");
            Main.retirerTous(x, "z");
            Check.equal(List.of("a", "b", "c"), x, "cible absente");
            List<String> y = l("k", "k");
            Main.retirerTous(y, "k");
            Check.equal(List.of(), y, "tout retirer");
        });
    }
}
