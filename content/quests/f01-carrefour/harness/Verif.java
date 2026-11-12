import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[] cas = {20, 15, 14, 5, 4, 0, -3};
        for (int pv : cas) {
            Check.test("Pour " + pv + " PV", () -> {
                var l = Check.lines(Check.runMain("Main", pv + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                String attendu = pv >= 15 ? "Voyageur en forme" : pv >= 5 ? "Voyageur blessé : tisane" : "Voyageur en danger : soins urgents";
                Check.equal(attendu, l.get(l.size() - 1), "Pour " + pv + " PV, dernière ligne");
            });
        }
    }
}
