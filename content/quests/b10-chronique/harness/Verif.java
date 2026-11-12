
import fr.javaquest.runner.Check;
import java.lang.reflect.Field;
import java.util.*;

class Verif {
    static Chronique avec(int cap, String... evts) {
        Chronique c = new Chronique(cap);
        for (String e : evts) c.noter(e);
        return c;
    }
    static void verifier() {
        Check.test("noter / recents : ordre du plus récent au plus ancien", () -> {
            Check.equal(List.of("b", "a"), avec(3, "a", "b").recents(), "recents après a, b");
            Check.equal(List.of(), new Chronique(2).recents(), "chronique vide");
        });
        Check.test("capacité : le plus ancien disparaît", () -> {
            Check.equal(List.of("d", "c", "b"), avec(3, "a", "b", "c", "d").recents(), "capacité 3, quatre événements");
            Check.equal(List.of("z"), avec(1, "x", "y", "z").recents(), "capacité 1");
        });
        Check.test("parLongueur : décroissante puis alphabétique", () -> {
            Chronique c = avec(4, "orc", "gobelin", "elfe", "nain");
            Check.equal(List.of("gobelin", "elfe", "nain", "orc"), c.parLongueur(), "parLongueur");
            Check.equal(List.of("nain", "elfe", "gobelin", "orc"), c.recents(), "parLongueur ne doit pas réordonner la chronique");
        });
        Check.test("copies indépendantes", () -> {
            Chronique c = avec(3, "a", "b");
            c.recents().clear();
            c.parLongueur().clear();
            Check.equal(List.of("b", "a"), c.recents(), "la chronique ne doit pas changer si on modifie une copie");
        });
        Check.test("stockage dans une LinkedList", () -> {
            Chronique c = avec(2, "a", "b");
            boolean trouve = false;
            for (Field f : Chronique.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.get(c) instanceof LinkedList) trouve = true;
            }
            Check.isTrue(trouve, "Stocke les événements dans une LinkedList (addFirst / removeLast).");
        });
    }
}
