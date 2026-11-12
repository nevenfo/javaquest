import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        String[] noms = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int d : new int[]{0, 6, 7, 17, 100}) {
            Check.test("Après " + d + " jours", () -> {
                var l = Check.lines(Check.runMain("Main", d + "\n"));
                Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes.");
                int k = l.size();
                Check.equal((d / 7) + " semaines et " + (d % 7) + " jours", l.get(k - 2), "Première ligne");
                Check.equal("Jour : " + noms[d % 7], l.get(k - 1), "Deuxième ligne");
            });
        }
    }
}
