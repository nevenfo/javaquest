import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Sans argument ou avec un seul", () -> {
            Check.equal("Usage : <prix> <quantite>", Check.lines(Check.runMain("Main", "")).get(0), "Sans argument");
            Check.equal("Usage : <prix> <quantite>", Check.lines(Check.runMain("Main", "", "3")).get(0), "Un seul argument");
        });
        String[][] cas = {{"2.5", "3"}, {"0.5", "4"}, {"4", "0"}, {"1.25", "10"}};
        for (String[] a : cas) {
            Check.test("Arguments " + a[0] + " " + a[1], () -> {
                double t = Double.parseDouble(a[0]) * Integer.parseInt(a[1]);
                var l = Check.lines(Check.runMain("Main", "", a));
                Check.equal(2, l.size(), "Nombre de lignes");
                Check.equal("Total : " + t, l.get(0), "Première ligne");
                Check.equal("Arrondi bas : " + (int) t, l.get(1), "Deuxième ligne");
            });
        }
    }
}
