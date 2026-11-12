import fr.javaquest.runner.Check;

class Verif {
    static void essai(int... v) {
        Check.test("Poids " + java.util.Arrays.toString(v), () -> {
            StringBuilder in = new StringBuilder();
            for (int x : v) in.append(x).append("\n");
            in.append("0\n");
            int[] t = v.clone();
            java.util.Arrays.sort(t);
            double med = t.length % 2 == 1 ? t[t.length / 2] : (t[t.length / 2 - 1] + t[t.length / 2]) / 2.0;
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            String m = l.get(l.size() - 1);
            Check.isTrue(m.startsWith("Médiane : "), "La dernière ligne doit commencer par « Médiane : ». Obtenu : " + m);
            double x = Double.NaN;
            try { x = Double.parseDouble(m.substring("Médiane : ".length()).trim()); }
            catch (NumberFormatException e) { Check.fail("La médiane doit être un nombre. Obtenu : " + m); }
            Check.near(med, x, 1e-6, "Médiane");
        });
    }

    static void verifier() {
        essai(5);
        essai(3, 1);
        essai(9, 1, 5);
        essai(4, 1, 3, 2);
        essai(-3, -7, -1, -9, -4);
        int[] grand = new int[25];
        int g = 12345;
        for (int i = 0; i < grand.length; i++) { g = (g * 1103515245 + 12345) & 0x7fffffff; grand[i] = g % 200 - 100; if (grand[i] == 0) grand[i] = 7; }
        essai(grand);
    }
}
