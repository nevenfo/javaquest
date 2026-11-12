import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<Integer> liste(Integer... v) { return new ArrayList<>(Arrays.asList(v)); }

    static void perimees(List<Integer> avant, List<Integer> apres) {
        Check.test("retirerPerimees " + avant, () -> {
            List<Integer> l = new ArrayList<>(avant);
            Main.retirerPerimees(l);
            Check.equal(apres, l, "fiches restantes après retirerPerimees(" + avant + ")");
        });
    }

    static void cote(List<Integer> avant, int c, List<Integer> apres) {
        Check.test("retirerCote " + avant + ", cote " + c, () -> {
            List<Integer> l = new ArrayList<>(avant);
            Main.retirerCote(l, c);
            Check.equal(apres, l, "fiches restantes après retirerCote(" + avant + ", " + c + ")");
        });
    }

    static void verifier() {
        perimees(liste(3, -1, 4, -2, -5, 6), liste(3, 4, 6));
        perimees(liste(-1, -2, -3), liste());
        perimees(liste(), liste());
        perimees(liste(5, 7), liste(5, 7));
        perimees(liste(-8), liste());
        perimees(liste(0, -1, -1, 0), liste(0, 0));
        cote(liste(10, 3, 7, 3), 3, liste(10, 7, 3));
        cote(liste(5, 6), 1, liste(5, 6));
        cote(liste(1, 2), 9, liste(1, 2));
        cote(liste(4), 4, liste());
        cote(liste(), 2, liste());
        cote(liste(0, 5, 0), 0, liste(5, 0));
        cote(liste(7, 8, 9), 2, liste(7, 8, 9));
    }
}
