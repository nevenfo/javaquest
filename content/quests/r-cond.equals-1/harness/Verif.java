import fr.javaquest.runner.Check;

class Verif {
    static void essai(String a, String b) {
        Check.test("« " + a + " » et « " + b + " »", () -> {
            var l = Check.lines(Check.runMain("Main", a + "\n" + b + "\n"));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal(a.equals(b) ? "Identiques" : "Différents", l.get(l.size() - 1), "Réponse");
        });
    }

    static void verifier() {
        essai("lune", "lune");
        essai("lune", "Lune");
        essai("a", "ab");
        essai("sol", "sol");
        essai("bois", "boit");
    }
}
