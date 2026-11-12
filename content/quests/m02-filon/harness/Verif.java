import fr.javaquest.runner.Check;

class Verif {
    static void essai(int depart, int objectif) {
        Check.test("Départ " + depart + ", objectif " + objectif, () -> {
            int v = depart, j = 0;
            while (v < objectif) { v *= 2; j++; }
            var l = Check.lines(Check.runMain("Main", depart + "\n" + objectif + "\n"));
            Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes (Jours puis Volume). Obtenu : " + l);
            Check.equal("Jours : " + j, l.get(l.size() - 2), "Avant-dernière ligne");
            Check.equal("Volume : " + v, l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai(1, 1);
        essai(1, 100);
        essai(3, 10);
        essai(5, 5);
        essai(7, 1);
        essai(2, 1000000);
    }
}
