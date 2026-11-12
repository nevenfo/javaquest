import fr.javaquest.runner.Check;

class Verif {
    static void essai(String in, int invalides, double v) {
        Check.test("Saisies " + in.replace("\n", " / "), () -> {
            var l = Check.lines(Check.runMain("Main", in));
            long n = l.stream().filter(s -> s.equals("Poids invalide.")).count();
            Check.equal((long) invalides, n, "Nombre de messages « Poids invalide. »");
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            String s = l.get(l.size() - 1);
            Check.isTrue(s.startsWith("Poids : "), "La dernière ligne doit commencer par « Poids : ». Obtenu : " + s);
            double x = Double.NaN;
            try { x = Double.parseDouble(s.substring(8).trim()); }
            catch (NumberFormatException e) { Check.fail("Poids illisible : " + s); }
            Check.near(v, x, 1e-9, "Poids");
        });
    }

    static void verifier() {
        essai("2.5\n", 0, 2.5);
        essai("beaucoup\n2.5\n", 1, 2.5);
        essai("x\n,5\n0.75\n", 2, 0.75);
        essai("pas\n-3.5\n", 1, -3.5);
    }
}
