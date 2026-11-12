
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[] minutes = {135, 59, 60, 0, 1000};
        double[] tarifs = {12.5, 10.0, 7.5, 8.0, 2.4};
        for (int i = 0; i < minutes.length; i++) {
            int m = minutes[i];
            double t = tarifs[i];
            Check.test("Durée " + m + " min, tarif " + t, () -> {
                var l = Check.lines(Check.runMain("Main", m + "\n" + t + "\n"));
                Check.isTrue(l.size() >= 3, "Il faut trois lignes (Heures, Minutes, Salaire). Obtenu : " + l);
                Check.equal("Heures : " + (m / 60), l.get(l.size() - 3), "Ligne Heures");
                Check.equal("Minutes : " + (m % 60), l.get(l.size() - 2), "Ligne Minutes");
                String s = l.get(l.size() - 1);
                Check.isTrue(s.startsWith("Salaire : "), "La dernière ligne doit commencer par « Salaire : ». Obtenu : " + s);
                double v = Double.NaN;
                try { v = Double.parseDouble(s.substring("Salaire : ".length()).trim()); }
                catch (NumberFormatException e) { Check.fail("Le salaire doit être un nombre. Obtenu : " + s); }
                Check.near(m / 60.0 * t, v, 1e-6, "Salaire (les minutes comptent aussi)");
            });
        }
    }
}
