import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{0, 1, 4, 9}) {
            Check.test("Couloir de " + n + " cases", () -> {
                Golem.reinitialiser();
                Check.runMain("Main", n + "\n");
                Check.equal(n, Golem.position(), "Position finale du golem");
                Check.equal(n, Golem.creuses(), "Nombre de cases creusées");
            });
        }
    }
}
