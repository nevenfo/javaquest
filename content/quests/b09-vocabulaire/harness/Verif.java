
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("mots : cas simple", () -> {
            Check.equal(List.of("le", "chat", "et", "le", "chien"), Main.mots("Le chat et le chien"), "mots(\"Le chat et le chien\")");
        });
        Check.test("mots : blancs multiples, début et fin, tabulation", () -> {
            Check.equal(List.of("or", "fer"), Main.mots("  Or   fer  "), "mots avec espaces multiples");
            Check.equal(List.of("a", "b"), Main.mots("a\tb"), "mots séparés par une tabulation");
        });
        Check.test("mots : texte vide ou blanc", () -> {
            Check.equal(List.of(), Main.mots(""), "mots(\"\")");
            Check.equal(List.of(), Main.mots("   "), "mots(\"   \")");
        });
        Check.test("distincts", () -> {
            Check.equal(4, Main.distincts("Le chat et le chien"), "distincts : le chat et le chien");
            Check.equal(1, Main.distincts("Ab aB AB"), "distincts : casse ignorée");
            Check.equal(0, Main.distincts(""), "distincts du texte vide");
            Check.equal(2, Main.distincts("x y x y x"), "distincts : deux mots répétés");
        });
        Check.test("frequences", () -> {
            Check.equal(Map.of("a", 2, "b", 1), Main.frequences("a b a"), "frequences(\"a b a\")");
            Check.equal(Map.of("ab", 3), Main.frequences("Ab aB AB"), "frequences : casse ignorée");
            Check.equal(Map.of(), Main.frequences(""), "frequences du texte vide");
            Check.equal(Map.of("le", 2, "chat", 1), Main.frequences(" le  chat le "), "frequences avec espaces multiples");
        });
    }
}
