import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (int n : new int[]{0, 2, 3, 7, 10}) {
            Check.test("Couloir de " + n + " cases", () -> {
                Golem.reinitialiser();
                Check.runMain("Main", n + "\n");
                Check.equal(n, Golem.position(), "Position finale du golem");
                Check.equal(n, Golem.creuses(), "Nombre de cases creusées");
                var voulu = new java.util.ArrayList<Integer>();
                for (int c = 3; c <= n; c += 3) voulu.add(c);
                Check.equal(voulu, Golem.positionsPierres(), "Cases où des pierres ont été posées");
            });
        }
    }
}
