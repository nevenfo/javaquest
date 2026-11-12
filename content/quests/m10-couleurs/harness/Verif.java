import fr.javaquest.runner.Check;

class Verif {
    static void essai(int... v) {
        Check.test("Couleurs " + java.util.Arrays.toString(v), () -> {
            int[] c = new int[5];
            StringBuilder in = new StringBuilder(v.length + "\n");
            for (int x : v) { c[x]++; in.append(x).append("\n"); }
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= 5, "Il faut afficher cinq lignes (couleurs 0 à 4). Obtenu : " + l);
            for (int k = 0; k < 5; k++)
                Check.equal(k + " : " + c[k], l.get(l.size() - 5 + k), "Ligne de la couleur " + k);
        });
    }

    static void verifier() {
        essai(1, 2, 2, 4, 0, 4);
        essai(3);
        essai(0, 0, 0, 0, 0);
        essai(4, 4, 4, 4);
        essai(0, 1, 2, 3, 4, 3, 2, 1, 0);
    }
}
