
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[][] cas = {{0, 0, 3, 4, 5}, {1, 1, 1, 1, 0}, {-2, -3, 1, 1, 5}, {0, 0, 0, 7, 7}, {5, 5, -5, 5, 10}};
        for (int[] c : cas) {
            Check.test("Distance (" + c[0] + "," + c[1] + ") -> (" + c[2] + "," + c[3] + ")", () -> {
                Rivet a = new Rivet(c[0], c[1]), b = new Rivet(c[2], c[3]);
                Check.near(c[4], a.distanceTo(b), 1e-9, "a.distanceTo(b)");
                Check.near(c[4], b.distanceTo(a), 1e-9, "b.distanceTo(a) (la distance est symétrique)");
            });
        }
        Check.test("Distance non entière", () -> Check.near(Math.sqrt(2), new Rivet(0, 0).distanceTo(new Rivet(1, 1)), 1e-9, "(0,0) -> (1,1)"));
        Check.test("deplacer renvoie un nouveau rivet décalé", () -> {
            Rivet a = new Rivet(1, 1);
            Rivet b = a.deplacer(2, -3);
            Check.isTrue(b != null && b != a, "deplacer doit renvoyer un nouvel objet Rivet.");
            Check.equal(3, b.getX(), "x du rivet déplacé");
            Check.equal(-2, b.getY(), "y du rivet déplacé");
            Check.equal(1, a.getX(), "x du rivet d'origine (inchangé)");
            Check.equal(1, a.getY(), "y du rivet d'origine (inchangé)");
        });
        Check.test("deplacer(0, 0)", () -> {
            Rivet b = new Rivet(4, 9).deplacer(0, 0);
            Check.equal(4, b.getX(), "x"); Check.equal(9, b.getY(), "y");
        });
    }
}
