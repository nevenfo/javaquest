import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        for (String m : new String[]{"elfe", "", "e", "abc", "eeee", "Evee", "grimoire"}) {
            Check.test("Mot « " + m + " »", () -> {
                int n = 0;
                for (char c : m.toCharArray()) if (c == 'e') n++;
                var l = Check.lines(Check.runMain("Main", m + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal("Lettres e : " + n, l.get(l.size() - 1), "Pour « " + m + " »");
            });
        }
    }
}
