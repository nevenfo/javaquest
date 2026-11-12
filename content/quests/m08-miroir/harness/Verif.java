import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (String m : new String[]{"", "a", "radar", "abba", "abca", "ab", "kayak", "Kayak", "esope reste ici et se repose", "ressasser", "abcdba"}) {
            Check.test("Mot « " + m + " »", () -> {
                boolean miroir = new StringBuilder(m).reverse().toString().equals(m);
                var l = Check.lines(Check.runMain("Main", m + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal(miroir ? "oui" : "non", l.get(l.size() - 1), "Pour « " + m + " »");
            });
        }
    }
}
