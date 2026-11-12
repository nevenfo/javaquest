import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("La formule « Ouvre-toi ! » est prononcée exactement une fois", () -> {
            String out = Check.runMain("Main", "");
            Check.equal(java.util.List.of("Ouvre-toi !"), Check.lines(out), "Lignes affichées");
        });
    }
}
