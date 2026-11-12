
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        int[][][] cas = {
            {{1, 4, 9}, {2, 4, 10, 11}, {1, 2, 4, 4, 9, 10, 11}},
            {{}, {3, 5}, {3, 5}},
            {{3, 5}, {}, {3, 5}},
            {{}, {}, {}},
            {{-5, 0, 7}, {-6, -5, 8}, {-6, -5, -5, 0, 7, 8}},
            {{1, 2, 3}, {10, 20}, {1, 2, 3, 10, 20}},
            {{10, 20}, {1, 2, 3}, {1, 2, 3, 10, 20}},
            {{1000, 2000}, {1000}, {1000, 1000, 2000}},
        };
        for (int[][] c : cas)
            Check.test("fusion(" + Arrays.toString(c[0]) + ", " + Arrays.toString(c[1]) + ")", () -> {
                List<Integer> a = new ArrayList<>(), b = new ArrayList<>(), att = new ArrayList<>();
                for (int x : c[0]) a.add(x);
                for (int x : c[1]) b.add(x);
                for (int x : c[2]) att.add(x);
                List<Integer> r = Main.fusion(a, b);
                Check.equal(att, r, "Résultat");
                Check.equal(c[0].length, a.size(), "Taille de la première liste (elle ne doit pas changer)");
                Check.equal(c[1].length, b.size(), "Taille de la seconde liste (elle ne doit pas changer)");
            });
    }
}
