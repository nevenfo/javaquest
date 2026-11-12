import fr.javaquest.runner.Check;

class Verif {
    static void essai(String m, String c) {
        Check.test("« " + m + " » et « " + c + " »", () -> {
            var l = Check.lines(Check.runMain("Main", m + "\n" + c + "\n"));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal("Indice : " + m.indexOf(c.charAt(0)), l.get(l.size() - 1), "Dernière ligne");
        });
    }

    static void verifier() {
        essai("banane", "n");
        essai("banane", "z");
        essai("", "a");
        essai("a", "a");
        essai("abc", "c");
        essai("aaa", "a");
    }
}
