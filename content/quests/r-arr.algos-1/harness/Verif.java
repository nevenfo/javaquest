import fr.javaquest.runner.Check;

class Verif {
    static void essai(int... v) {
        Check.test("Valeurs " + java.util.Arrays.toString(v), () -> {
            StringBuilder in = new StringBuilder(v.length + "\n");
            int min = v[0], max = v[0];
            for (int x : v) { in.append(x).append("\n"); min = Math.min(min, x); max = Math.max(max, x); }
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal("Écart : " + (max - min), l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai(4, 10, 2);
        essai(5);
        essai(-5, -2, -9);
        essai(4, 4, 4);
        essai(-3, 8);
    }
}
