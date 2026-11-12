
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        int[] cas = {5, 0, 1, 33};
        for (int n : cas) {
            Check.test("Pour " + n + " seaux", () -> {
                var l = Check.lines(Check.runMain("Main", n + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal("Litres : " + n * 12, l.get(l.size() - 1), "Dernière ligne");
            });
        }
    }
}
