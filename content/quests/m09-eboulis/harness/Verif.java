import fr.javaquest.runner.Check;

class Verif {
    static void essai(int... v) {
        Check.test("Filons " + java.util.Arrays.toString(v), () -> {
            StringBuilder in = new StringBuilder(v.length + "\n");
            for (int x : v) in.append(x).append("\n");
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= v.length, "Il faut afficher " + v.length + " valeurs. Obtenu : " + l);
            for (int i = 0; i < v.length; i++) {
                Check.equal(String.valueOf(v[v.length - 1 - i]), l.get(l.size() - v.length + i), "Valeur affichée n° " + (i + 1) + " (en partant de la fin des lectures)");
            }
        });
    }

    static void verifier() {
        essai(4, 8, 15);
        essai(9);
        essai(-1, 0, 7, 7, 2);
        essai(1, 2);
    }
}
