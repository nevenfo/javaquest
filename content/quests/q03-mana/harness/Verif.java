import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Le grimoire affiche la réserve finale", () -> {
            var lignes = Check.lines(Check.runMain("Main", ""));
            Check.isTrue(!lignes.isEmpty(), "Rien n'est affiché : la dernière ligne doit afficher la réserve.");
            Check.equal("Mana restant : 11", lignes.get(lignes.size() - 1), "Dernière ligne");
        });
    }
}
