import fr.javaquest.runner.Check;

class Verif {
    static String panneau(String d) {
        return switch (d) {
            case "nord" -> "Vers la Tour";
            case "sud" -> "Vers Javelune";
            case "est" -> "Vers les Mines de Boucle-Profonde";
            case "ouest" -> "Vers la mer";
            default -> "Sentier inconnu";
        };
    }

    static void verifier() {
        String[] cas = {"nord", "sud", "est", "ouest", "Est", "haut", "sud-est", ""};
        for (String d : cas) {
            Check.test("Direction « " + d + " »", () -> {
                var l = Check.lines(Check.runMain("Main", d + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal(panneau(d), l.get(l.size() - 1), "Pour « " + d + " »");
            });
        }
    }
}
