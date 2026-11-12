import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Arguments manquants", () -> {
            Check.equal("Usage : <direction> <distance>", Check.lines(Check.runMain("Main", "")).get(0), "Sans argument");
            Check.equal("Usage : <direction> <distance>", Check.lines(Check.runMain("Main", "", "nord")).get(0), "Un seul argument");
        });
        Check.test("Direction inconnue", () -> Check.equal("Direction inconnue : haut", Check.lines(Check.runMain("Main", "", "haut", "3")).get(0), "Ligne affichée"));
        String[][] cas = {{"nord", "2.5", "(0.0, 2.5)"}, {"sud", "4", "(0.0, -4.0)"}, {"est", "0", "(0.0, 0.0)"}, {"ouest", "1.5", "(-1.5, 0.0)"}};
        for (String[] c : cas) {
            Check.test("Direction " + c[0] + " " + c[1], () -> Check.equal("Position : " + c[2], Check.lines(Check.runMain("Main", "", c[0], c[1])).get(0), "Ligne affichée"));
        }
    }
}
