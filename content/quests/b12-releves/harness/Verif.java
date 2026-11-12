
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("lire : lignes propres", () -> {
            Check.equal(List.of(new Chasseur("Ada", 12), new Chasseur("Zoe", 7)), Main.lire(List.of("Ada:12", "Zoe:7")), "lire([Ada:12, Zoe:7])");
        });
        Check.test("lire : espaces et lignes vides", () -> {
            Check.equal(List.of(new Chasseur("Ada", 12), new Chasseur("Bob", 12)), Main.lire(List.of(" Ada : 12 ", "", "Bob :12", "   ")), "lire avec espaces et lignes vides");
            Check.equal(List.of(), Main.lire(List.of()), "lire([])");
        });
        Check.test("classement : prises décroissantes puis nom", () -> {
            Check.equal(List.of("Ada (12)", "Bob (12)", "Zoe (7)"), Main.classement(List.of(" Ada : 12 ", "Zoe:7", "", "Bob :12")), "classement");
            Check.equal(List.of("Cléo (30)", "Max (2)", "Ned (1)", "Ola (0)"), Main.classement(List.of("Ola:0", "Max:2", "Cléo:30", "Ned:1")), "classement avec une seule meilleure prise");
        });
        Check.test("classement : limites", () -> {
            Check.equal(List.of(), Main.classement(List.of()), "classement vide");
            Check.equal(List.of("Seul (5)"), Main.classement(List.of("Seul:5")), "un seul chasseur");
        });
    }
}
