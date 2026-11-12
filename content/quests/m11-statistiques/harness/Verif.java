import fr.javaquest.runner.Check;

class Verif {
    static void essai(int... v) {
        Check.test("Mesures " + java.util.Arrays.toString(v), () -> {
            StringBuilder in = new StringBuilder(v.length + "\n");
            int min = v[0], max = v[0]; long s = 0;
            for (int x : v) { in.append(x).append("\n"); min = Math.min(min, x); max = Math.max(max, x); s += x; }
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= 3, "Il faut afficher trois lignes (Min, Max, Moyenne). Obtenu : " + l);
            Check.equal("Min : " + min, l.get(l.size() - 3), "Ligne Min");
            Check.equal("Max : " + max, l.get(l.size() - 2), "Ligne Max");
            String m = l.get(l.size() - 1);
            Check.isTrue(m.startsWith("Moyenne : "), "La dernière ligne doit commencer par « Moyenne : ». Obtenu : " + m);
            double x = Double.NaN;
            try { x = Double.parseDouble(m.substring("Moyenne : ".length()).trim()); }
            catch (NumberFormatException e) { Check.fail("La moyenne doit être un nombre. Obtenu : " + m); }
            Check.near((double) s / v.length, x, 1e-6, "Moyenne");
        });
    }

    static void verifier() {
        essai(3, 9, 1);
        essai(5);
        essai(-5, -2, -9);
        essai(4, 4, 4, 5);
        essai(1, 2);
    }
}
