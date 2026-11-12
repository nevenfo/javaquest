
import fr.javaquest.runner.Check;
import java.util.List;

class Verif {
    static void verifier() {
        int[][] cas = {{3, -4}, {0, 0}, {-12, 7}, {100, 2000}};
        for (int[] c : cas) {
            Check.test("toString de (" + c[0] + ", " + c[1] + ")", () -> {
                Check.equal("Rivet(" + c[0] + ", " + c[1] + ")", new Rivet(c[0], c[1]).toString(), "toString()");
            });
        }
        Check.test("Utilisation implicite (concaténation et liste)", () -> {
            Check.equal("Posé : Rivet(1, 2)", "Posé : " + new Rivet(1, 2), "Concaténation");
            Check.equal("[Rivet(1, 2), Rivet(0, 0)]", List.of(new Rivet(1, 2), new Rivet(0, 0)).toString(), "Liste de rivets");
        });
    }
}
