import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{0, 1, 5, 12, 13, 15, 20}) {
            Check.test("Factorielle de " + n, () -> {
                long f = 1;
                for (int i = 2; i <= n; i++) f *= i;
                String voulu = n + "! = " + f;
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                String obtenu = l.get(l.size() - 1);
                if (n >= 13 && !obtenu.equals(voulu))
                    Check.fail("Pour " + n + " : attendu « " + voulu + " », obtenu « " + obtenu + " ». Le résultat dépasse 2,1 milliards : ton type a-t-il assez de place ?");
                Check.equal(voulu, obtenu, "Dernière ligne");
            });
        }
    }
}
