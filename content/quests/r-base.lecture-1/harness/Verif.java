import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        String[] noms = {"Mira", "Tobias", "Ada", "Zoe"};
        int[] annees = {2005, 2026, 1990, 2000};
        for (int i = 0; i < noms.length; i++) {
            String n = noms[i];
            int a = annees[i];
            Check.test(n + ", né·e en " + a, () -> {
                var l = Check.lines(Check.runMain("Main", n + "\n" + a + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal("Salut " + n + " ! Tu as " + (2026 - a) + " ans.", l.get(l.size() - 1), "Dernière ligne");
            });
        }
    }
}
