import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Object[][] cas = {
            {"sapin-d'or\n2\n", 0, "Trop sombre"},
            {"sapin-d'or\n3\n", 0, "Bien éclairé"},
            {"sapin-d'or\nx\n5\n", 1, "Bien éclairé"},
            {"sapin-d'or\n6\n", 0, "Bien éclairé"},
            {"sapin-d'or\n7\n", 0, "Aveuglant"},
            {"sapin-d'or\na\n\n-2\n", 2, "Trop sombre"},
        };
        for (Object[] c : cas) {
            String in = (String) c[0];
            int invalides = (Integer) c[1];
            String fin = (String) c[2];
            Check.test("Bon mot de passe, saisies " + in.replace("\n", " / "), () -> {
                var l = Check.lines(Check.runMain("Main", in));
                long n = l.stream().filter(s -> s.equals("Saisie invalide.")).count();
                Check.equal((long) invalides, n, "Nombre de messages « Saisie invalide. »");
                Check.isTrue(l.size() >= 2, "Il faut afficher « Grille ouverte. » puis la luminosité. Obtenu : " + l);
                Check.equal("Grille ouverte.", l.get(l.size() - 2), "Avant-dernière ligne");
                Check.equal(fin, l.get(l.size() - 1), "Dernière ligne");
            });
        }
        String[] mauvais = {"Sapin-d'or", "sapin", "", "sapin-d'or "};
        for (String mp : mauvais) {
            Check.test("Mauvais mot de passe « " + mp + " »", () -> {
                var l = Check.lines(Check.runMain("Main", mp + "\n5\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal("Grille fermée.", l.get(l.size() - 1), "Dernière ligne");
                Check.isTrue(l.stream().noneMatch(s -> s.contains("Grille ouverte")), "La grille ne doit pas s'ouvrir avec un mauvais mot de passe.");
            });
        }
    }
}
