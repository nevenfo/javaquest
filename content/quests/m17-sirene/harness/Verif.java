import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        String[][] cas = {{"chat", "hibou"}, {}, {"sync"}, {"a", "eau", "banane", "rhum", "pizza"}, {"o", "o", "o", "o", "o", "o", "o", "o", "o", "o"}};
        for (String[] mots : cas) {
            Check.test(mots.length + " mot(s)", () -> {
                StringBuilder in = new StringBuilder();
                for (String m : mots) in.append(m).append("\n");
                in.append("FIN\n");
                var l = new java.util.ArrayList<String>();
                for (String s : Check.lines(Check.runMain("Main", in.toString()))) if (s.contains(" : ") && !s.contains("?")) l.add(s);
                int total = 0, max = 0;
                java.util.List<String> att = new java.util.ArrayList<>();
                for (String m : mots) {
                    int n = 0;
                    for (char c : m.toCharArray()) if ("aeiou".indexOf(c) >= 0) n++;
                    total += n; max = Math.max(max, n);
                    att.add(m + " : " + n);
                }
                att.add("Total : " + total);
                att.add("Maximum : " + max);
                Check.equal(att, l, "Lignes affichées");
            });
        }
    }
}
