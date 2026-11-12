
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Tri d'une liste d'apprentis", () -> {
            List<Apprenti> l = new ArrayList<>(List.of(new Apprenti("Zoé", 3), new Apprenti("Ali", 3), new Apprenti("Bob", 1), new Apprenti("Cléo", 7), new Apprenti("Ada", 3)));
            Collections.sort(l);
            Check.equal("[Bob(1), Ada(3), Ali(3), Zoé(3), Cléo(7)]", l.toString(), "Liste triée");
        });
        Check.test("Signes de compareTo", () -> {
            Apprenti petit = new Apprenti("X", 1), grand = new Apprenti("A", 9);
            Check.isTrue(petit.compareTo(grand) < 0, "Niveau 1 doit passer avant niveau 9 : compareTo négatif attendu.");
            Check.isTrue(grand.compareTo(petit) > 0, "Niveau 9 après niveau 1 : compareTo positif attendu.");
        });
        Check.test("Égalité", () -> {
            Check.equal(0, new Apprenti("Max", 4).compareTo(new Apprenti("Max", 4)), "compareTo de deux apprentis identiques");
        });
        Check.test("Départage par le nom", () -> {
            Check.isTrue(new Apprenti("Ada", 5).compareTo(new Apprenti("Ali", 5)) < 0, "À niveau égal, « Ada » passe avant « Ali ».");
            Check.isTrue(new Apprenti("Ali", 5).compareTo(new Apprenti("Ada", 5)) > 0, "À niveau égal, « Ali » passe après « Ada ».");
        });
        Check.test("Grands niveaux (pas de débordement)", () -> {
            Apprenti a = new Apprenti("A", 2_000_000_000), b = new Apprenti("B", -2_000_000_000);
            Check.isTrue(a.compareTo(b) > 0, "2 milliards est plus grand que -2 milliards : compareTo positif attendu (attention à la soustraction).");
            Check.isTrue(b.compareTo(a) < 0, "-2 milliards passe avant 2 milliards.");
        });
        Check.test("TreeSet", () -> {
            TreeSet<Apprenti> t = new TreeSet<>(List.of(new Apprenti("B", 2), new Apprenti("A", 2), new Apprenti("A", 2), new Apprenti("C", 1)));
            Check.equal("[C(1), A(2), B(2)]", t.toString(), "TreeSet d'apprentis");
        });
    }
}
