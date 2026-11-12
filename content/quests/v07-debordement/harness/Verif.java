
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[] jours = {1, 2, 10, 31, 32, 40, 62};
        for (int n : jours) {
            Check.test("Jour " + n, () -> {
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                long attendu = 1L << (n - 1);
                String voulu = "Jour " + n + " : " + attendu + " pièces";
                String obtenu = l.get(l.size() - 1);
                if (n >= 32 && !obtenu.equals(voulu))
                    Check.fail("Jour " + n + " : attendu « " + voulu + " », obtenu « " + obtenu + " ». Le nombre de pièces dépasse 2,1 milliards : ton type a-t-il assez de place ?");
                Check.equal(voulu, obtenu, "Dernière ligne");
            });
        }
    }
}
