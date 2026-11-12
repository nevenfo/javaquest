import fr.javaquest.runner.Check;

class Verif {
    static void essai(String in, int wagons, int total) {
        Check.test("Charges " + in.replace("\n", ", "), () -> {
            var l = Check.lines(Check.runMain("Main", in));
            Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes (Wagons puis Total). Obtenu : " + l);
            Check.equal("Wagons : " + wagons, l.get(l.size() - 2), "Avant-dernière ligne");
            Check.equal("Total : " + total, l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai("0\n", 0, 0);
        essai("5\n0\n", 1, 5);
        essai("3\n4\n10\n0\n", 3, 17);
        essai("7\n-2\n0\n", 2, 5);
        essai("-5\n-5\n8\n0\n", 3, -2);
    }
}
