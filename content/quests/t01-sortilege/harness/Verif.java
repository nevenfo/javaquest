
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Eclair respecte le contrat Sortilege", () -> {
            Check.isTrue(Sortilege.class.isAssignableFrom(Check.playerClass("Eclair")), "Eclair doit déclarer `implements Sortilege`.");
        });
        int[] niveaux = {1, 3, 0, 25};
        for (int n : niveaux)
            Check.test("Eclair de niveau " + n, () -> {
                Sortilege s = new Eclair(n);
                Check.equal("Éclair", s.nom(), "nom()");
                Check.equal(4 * n, s.degats(), "degats()");
            });
        Check.test("Deux éclairs indépendants", () -> {
            Sortilege a = new Eclair(2), b = new Eclair(5);
            Check.equal(8, a.degats(), "degats() du premier");
            Check.equal(20, b.degats(), "degats() du second");
        });
    }
}
