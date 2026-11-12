import fr.javaquest.runner.Check;

class Verif {
    static void essai(String titre, String saisies, int invalides, int valeur) {
        Check.test(titre, () -> {
            var l = Check.lines(Check.runMain("Main", saisies));
            long n = l.stream().filter(s -> s.equals("Ce n'est pas un nombre !")).count();
            Check.equal((long) invalides, n, "Nombre de messages « Ce n'est pas un nombre ! »");
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal("Fagots : " + valeur, l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai("Saisie valide", "5\n", 0, 5);
        essai("Une saisie invalide", "abc\n7\n", 1, 7);
        essai("Trois saisies invalides", "x\n3.5\n\n12\n", 3, 12);
        essai("Nombre négatif", "-4\n", 0, -4);
    }
}
