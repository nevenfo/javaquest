
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<String> l(String... t) { return new ArrayList<>(Arrays.asList(t)); }
    static void verifier() {
        Check.test("emprunter un titre présent", () -> {
            List<String> r = l("Ombres", "Runes", "Cendres");
            Check.equal(true, Main.emprunter(r, new String("Runes")), "emprunter(\"Runes\")");
            Check.equal(List.of("Ombres", "Cendres"), r, "Rayon après emprunt");
        });
        Check.test("emprunter un titre absent ou dans un rayon vide", () -> {
            List<String> r = l("A", "B");
            Check.equal(false, Main.emprunter(r, "Z"), "emprunter(\"Z\")");
            Check.equal(List.of("A", "B"), r, "Rayon inchangé");
            Check.equal(false, Main.emprunter(l(), "A"), "Rayon vide");
        });
        Check.test("emprunter ne retire que la première occurrence", () -> {
            List<String> r = l("A", "B", "A");
            Main.emprunter(r, "A");
            Check.equal(List.of("B", "A"), r, "Rayon après emprunt d'un doublon");
        });
        Check.test("retirerCourts : cas simple", () -> {
            List<String> r = l("Ab", "Runes", "Xy", "Zzz", "Q");
            Main.retirerCourts(r, 3);
            Check.equal(List.of("Runes", "Zzz"), r, "Rayon après retirerCourts(…, 3)");
        });
        Check.test("retirerCourts : titres courts consécutifs", () -> {
            List<String> r = l("a", "b", "c", "dddd", "e", "f");
            Main.retirerCourts(r, 2);
            Check.equal(List.of("dddd"), r, "Rayon après retirerCourts(…, 2) (attention à ne pas sauter d'éléments)");
        });
        Check.test("retirerCourts : rien à retirer, tout à retirer, liste vide", () -> {
            List<String> r = l("abc", "defg");
            Main.retirerCourts(r, 3);
            Check.equal(List.of("abc", "defg"), r, "Aucun titre trop court");
            Main.retirerCourts(r, 10);
            Check.equal(List.of(), r, "Tous les titres sont trop courts");
            List<String> v = l();
            Main.retirerCourts(v, 5);
            Check.equal(List.of(), v, "Rayon vide");
        });
    }
}
