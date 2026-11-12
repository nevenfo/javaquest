import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{4, 1, 0, 25}) {
            Check.test("Pour " + n + " fioles", () -> {
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(l.size() >= 3, "Il faut afficher trois lignes.");
                int k = l.size();
                Check.equal("Recette pour " + n + " fioles", l.get(k - 3), "Première ligne");
                Check.equal("Gouttes : " + (3 * n), l.get(k - 2), "Deuxième ligne");
                Check.equal("Pétales : " + (2 * n), l.get(k - 1), "Troisième ligne");
            });
        }
    }
}
