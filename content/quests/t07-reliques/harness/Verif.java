
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Accesseurs et toString", () -> {
            Relique r = new Relique("Sceptre", 12);
            Check.equal("Sceptre", r.getNom(), "getNom()");
            Check.equal(12, r.getPoids(), "getPoids()");
            Check.equal("Sceptre (12 kg)", r.toString(), "toString()");
        });
        Check.test("Tri : la plus lourde d'abord", () -> {
            List<Relique> l = new ArrayList<>(List.of(new Relique("Calice", 5), new Relique("Sceptre", 12), new Relique("Anneau", 5), new Relique("Plume", 1)));
            Collections.sort(l);
            Check.equal("[Sceptre (12 kg), Anneau (5 kg), Calice (5 kg), Plume (1 kg)]", l.toString(), "Liste triée");
        });
        Check.test("Départage alphabétique quand l'ordre d'entrée est inverse", () -> {
            List<Relique> l = new ArrayList<>(List.of(new Relique("Zèbre", 3), new Relique("Aube", 3), new Relique("Mire", 3)));
            Collections.sort(l);
            Check.equal("[Aube (3 kg), Mire (3 kg), Zèbre (3 kg)]", l.toString(), "À poids égal");
        });
        Check.test("compareTo : signes et égalité", () -> {
            Relique a = new Relique("A", 10), b = new Relique("B", 2);
            Check.isTrue(a.compareTo(b) < 0, "Une relique de 10 kg passe avant une relique de 2 kg.");
            Check.isTrue(b.compareTo(a) > 0, "Une relique de 2 kg passe après une relique de 10 kg.");
            Check.equal(0, new Relique("X", 4).compareTo(new Relique("X", 4)), "Même nom, même poids");
        });
        Check.test("Poids extrêmes", () -> {
            Relique lourd = new Relique("L", 2_000_000_000), neg = new Relique("N", -2_000_000_000);
            Check.isTrue(lourd.compareTo(neg) < 0, "2 milliards passe avant -2 milliards (pas de débordement).");
            Check.isTrue(neg.compareTo(lourd) > 0, "-2 milliards passe après 2 milliards.");
        });
        Check.test("Utilisable dans un TreeSet", () -> {
            TreeSet<Relique> t = new TreeSet<>();
            t.add(new Relique("B", 1)); t.add(new Relique("A", 1)); t.add(new Relique("A", 1)); t.add(new Relique("C", 9));
            Check.equal("[C (9 kg), A (1 kg), B (1 kg)]", t.toString(), "TreeSet");
        });
    }
}
