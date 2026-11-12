import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{0, 1, 3, 10}) {
            Check.test("Table de " + n + " lignes", () -> {
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                var table = l.stream().filter(s -> s.matches("7 x \\d+ = \\d+")).toList();
                var voulu = new java.util.ArrayList<String>();
                for (int k = 1; k <= n; k++) voulu.add("7 x " + k + " = " + 7 * k);
                Check.equal(voulu, table, "Lignes de la table (de la forme « 7 x k = résultat »)");
            });
        }
    }
}
