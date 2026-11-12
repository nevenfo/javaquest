
import fr.javaquest.runner.Check;
import java.lang.reflect.*;

class Verif {
    static int lire(Object o, String champ) throws Exception {
        Field f = o.getClass().getDeclaredField(champ);
        f.setAccessible(true);
        return f.getInt(o);
    }
    static void verifier() {
        int[][] cas = {{3, 4}, {0, 0}, {-2, 7}, {100, -100}};
        for (int[] c : cas) {
            Check.test("new Rivet(" + c[0] + ", " + c[1] + ") range ses deux coordonnées", () -> {
                Object r = new Rivet(c[0], c[1]);
                Check.equal(c[0], lire(r, "x"), "Champ x");
                Check.equal(c[1], lire(r, "y"), "Champ y");
            });
        }
        Check.test("Deux rivets sont indépendants", () -> {
            Object a = new Rivet(1, 2), b = new Rivet(8, 9);
            Check.equal(1, lire(a, "x"), "x du premier rivet");
            Check.equal(9, lire(b, "y"), "y du second rivet");
        });
    }
}
