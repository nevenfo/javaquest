import fr.javaquest.runner.Check;

class Verif {
    static String r(int j) { return j == 1 ? "Échec critique" : j >= 18 ? "Réussite critique" : j >= 10 ? "Réussite" : "Échec"; }

    static void verifier() {
        for (int j : new int[]{1, 2, 9, 10, 17, 18, 20}) {
            Check.test("Jet de " + j, () -> {
                var l = Check.lines(Check.runMain("Main", j + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal(r(j), l.get(l.size() - 1), "Pour un jet de " + j);
            });
        }
    }
}
