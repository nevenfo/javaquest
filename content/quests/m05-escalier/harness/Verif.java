import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{0, 1, 2, 5, 10, 1000}) {
            Check.test("Escalier de " + n + " étages", () -> {
                int somme = 0;
                for (int k = 1; k <= n; k++) somme += k * k;
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal("Pierres : " + somme, l.get(l.size() - 1), "Pour " + n + " étages");
            });
        }
    }
}
