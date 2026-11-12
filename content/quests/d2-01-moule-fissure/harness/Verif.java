import fr.javaquest.runner.Check;
import java.util.HashSet;
import java.util.Set;

class Verif {
    static void verifier() {
        Check.test("Le constructeur range bien nom et capacité", () -> {
            Moule m = new Moule(new String("cloche"), 12);
            Check.equal("cloche", m.getNom(), "getNom()");
            Check.equal(12, m.getCapacite(), "getCapacite()");
            Moule n = new Moule("fer", 3);
            Check.equal("fer", n.getNom(), "getNom() du second moule");
            Check.equal(3, n.getCapacite(), "getCapacite() du second moule");
        });
        Check.test("toString()", () -> {
            Check.equal("Moule[cloche, 12]", new Moule("cloche", 12).toString(), "toString()");
        });
        Check.test("equals : même contenu, objets String distincts", () -> {
            Object a = new Moule(new String("cloche"), 12), b = new Moule(new String("cloche"), 12);
            Check.isTrue(a.equals(b), "Deux moules de même nom (comparé avec equals, pas ==) et même capacité doivent être égaux.");
            Check.isTrue(a.equals(a), "Un moule doit être égal à lui-même.");
        });
        Check.test("equals : différences et cas limites", () -> {
            Object a = new Moule("cloche", 12);
            Check.isTrue(!a.equals(new Moule("cloche", 13)), "Capacités différentes : moules différents.");
            Check.isTrue(!a.equals(new Moule("lingot", 12)), "Noms différents : moules différents.");
            Check.isTrue(!a.equals(null), "equals(null) doit renvoyer false.");
            Check.isTrue(!a.equals("cloche"), "equals avec un autre type doit renvoyer false.");
        });
        Check.test("hashCode cohérent avec equals (HashSet)", () -> {
            Set<Moule> s = new HashSet<>();
            s.add(new Moule(new String("cloche"), 12));
            s.add(new Moule(new String("cloche"), 12));
            s.add(new Moule("lingot", 12));
            Check.equal(2, s.size(), "Taille du HashSet (le doublon ne doit pas compter)");
            Check.equal(new Moule(new String("x"), 1).hashCode(), new Moule(new String("x"), 1).hashCode(), "hashCode de moules égaux");
        });
    }
}
