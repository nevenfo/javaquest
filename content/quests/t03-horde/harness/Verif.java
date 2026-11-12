
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static Monstre m(String cri, int att) {
        return new Monstre() {
            public String cri() { return cri; }
            public int attaque() { return att; }
        };
    }
    static void verifier() {
        Check.test("Gobelin et Dragon", () -> {
            Monstre g = new Gobelin(), d = new Dragon(5);
            Check.equal("Grr !", g.cri(), "Cri du gobelin");
            Check.equal(3, g.attaque(), "Attaque du gobelin");
            Check.equal("ROAAR !", d.cri(), "Cri du dragon");
            Check.equal(15, d.attaque(), "Attaque d'un dragon d'envergure 5");
            Check.equal(10, new Dragon(0).attaque(), "Attaque d'un dragon d'envergure 0");
        });
        Check.test("attaqueTotale avec des monstres du harnais", () -> {
            List<Monstre> h = List.of(m("a", 4), new Gobelin(), m("b", 10), new Dragon(2));
            Check.equal(4 + 3 + 10 + 12, Main.attaqueTotale(h), "attaqueTotale");
            Check.equal(0, Main.attaqueTotale(List.of()), "attaqueTotale d'une horde vide");
            Check.equal(3, Main.attaqueTotale(List.of(new Gobelin())), "attaqueTotale d'un seul gobelin");
        });
        Check.test("cris", () -> {
            Check.equal("Grr ! hop ROAAR !", Main.cris(List.of(new Gobelin(), m("hop", 1), new Dragon(1))), "cris");
            Check.equal("", Main.cris(List.of()), "cris d'une horde vide");
            Check.equal("seul", Main.cris(List.of(m("seul", 2))), "cris d'un seul monstre");
        });
        Check.test("plusFort", () -> {
            Monstre a = m("a", 5), b = m("b", 9), c = m("c", 9);
            Check.isTrue(Main.plusFort(List.of(a, b, c)) == b, "Le plus fort est « b » (attaque 9, premier des deux à 9).");
            Check.isTrue(Main.plusFort(List.of(a)) == a, "Un seul monstre : c'est lui.");
            Monstre d = new Dragon(0);
            Check.isTrue(Main.plusFort(List.of(new Gobelin(), d, m("x", 8))) == d, "Le dragon (10) domine gobelin (3) et x (8).");
            Check.isTrue(Main.plusFort(List.of()) == null, "Horde vide : null.");
        });
    }
}
