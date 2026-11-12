
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("equals via une référence Object", () -> {
            Object a = new Rivet(2, 3), b = new Rivet(2, 3);
            Check.isTrue(a.equals(b), "Deux rivets (2, 3) devraient être égaux quand on les compare comme des Object : redéfinis equals(Object).");
            Check.isTrue(!a.equals(new Rivet(3, 2)), "(2, 3) et (3, 2) sont des rivets différents.");
            Check.isTrue(!a.equals(new Rivet(2, 4)), "(2, 3) et (2, 4) sont des rivets différents.");
        });
        Check.test("equals avec null et un autre type", () -> {
            Object a = new Rivet(0, 0);
            Check.isTrue(!a.equals(null), "equals(null) doit répondre false.");
            Check.isTrue(!a.equals("(0, 0)"), "Un rivet n'est pas égal à une chaîne (et ne doit pas planter).");
            Check.isTrue(a.equals(a), "Un rivet est égal à lui-même.");
        });
        Check.test("hashCode cohérent avec equals", () -> {
            Check.equal(new Rivet(5, -1).hashCode(), new Rivet(5, -1).hashCode(), "hashCode de deux rivets égaux");
            Check.equal(new Rivet(0, 0).hashCode(), new Rivet(0, 0).hashCode(), "hashCode de deux rivets (0, 0)");
        });
        Check.test("Le registre HashSet ne contient plus de doublon", () -> {
            Set<Rivet> registre = new HashSet<>();
            registre.add(new Rivet(1, 1));
            registre.add(new Rivet(1, 1));
            registre.add(new Rivet(1, 2));
            registre.add(new Rivet(1, 2));
            registre.add(new Rivet(-1, -1));
            Check.equal(3, registre.size(), "Taille du registre");
            Check.isTrue(registre.contains(new Rivet(-1, -1)), "contains(new Rivet(-1, -1)) doit être vrai.");
        });
        Check.test("Anciennes fonctions intactes", () -> {
            Check.equal(4, new Rivet(4, 9).getX(), "getX()");
        });
    }
}
