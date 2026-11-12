import fr.javaquest.runner.Check;

class Verif {
    static void essai(String titre, String saisies, int invalides, int hors, int valeur) {
        Check.test(titre, () -> {
            var l = Check.lines(Check.runMain("Main", saisies));
            long a = l.stream().filter(s -> s.equals("Saisie invalide.")).count();
            long b = l.stream().filter(s -> s.equals("Entre 1 et 10 !")).count();
            Check.equal((long) invalides, a, "Nombre de messages « Saisie invalide. »");
            Check.equal((long) hors, b, "Nombre de messages « Entre 1 et 10 ! »");
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal("Merci : " + valeur + " glands", l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai("Saisie directe", "4\n", 0, 0, 4);
        essai("Texte puis hors bornes puis valide", "x\n11\n4\n", 1, 1, 4);
        essai("Bornes incluses (1)", "1\n", 0, 0, 1);
        essai("Bornes incluses (10)", "10\n", 0, 0, 10);
        essai("Zéro et négatif refusés", "0\n-5\n2\n", 0, 2, 2);
        essai("Plusieurs textes", "\nabc\n2.5\n7\n", 3, 0, 7);
    }
}
