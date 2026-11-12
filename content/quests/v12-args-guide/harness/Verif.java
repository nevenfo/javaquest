import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        String[][] cas = {{}, {"Mira"}, {"Tobias", "Ada"}, {"Zoe", "x", "y"}};
        for (String[] a : cas) {
            Check.test(a.length + " argument(s)", () -> {
                var l = Check.lines(Check.runMain("Main", "", a));
                Check.equal(2, l.size(), "Nombre de lignes affichées");
                Check.equal("Salut, " + (a.length == 0 ? "inconnu" : a[0]) + " !", l.get(0), "Première ligne");
                Check.equal("Arguments reçus : " + a.length, l.get(1), "Deuxième ligne");
            });
        }
    }
}
