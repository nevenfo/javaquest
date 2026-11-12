import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[] cas = {23, 7, 10, 0, 1000};
        for (int n : cas) {
            Check.test("Pour " + n + " sacs", () -> {
                var lignes = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(lignes.size() >= 2, "Il faut afficher deux lignes (caisses puis sacs restants). Obtenu : " + lignes);
                Check.equal("Caisses : " + (n / 10), lignes.get(lignes.size() - 2), "Avant-dernière ligne");
                Check.equal("Sacs restants : " + (n % 10), lignes.get(lignes.size() - 1), "Dernière ligne");
            });
        }
    }
}
