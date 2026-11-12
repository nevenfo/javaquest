import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("La pancarte", () -> {
            var l = Check.lines(Check.runMain("Main", ""));
            Check.equal(java.util.List.of("Bienvenue à Javelune", "Habitants : 1200", "Fondée il y a 300 ans"), l, "Lignes affichées");
        });
    }
}
