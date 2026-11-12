import fr.javaquest.runner.Check;

class Verif {
    static Object appeler(String nom, Object... args) throws Throwable {
        Class<?> c = Check.playerClass("Main");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(nom) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                try {
                    return m.invoke(null, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }
        Check.fail("La méthode " + nom + " (avec " + args.length + " paramètre(s)) est introuvable dans Main.");
        return null;
    }

    static int[] comptes(String s) {
        int[] t = new int[26];
        for (char c : s.toCharArray()) if (c >= 'a' && c <= 'z') t[c - 'a']++;
        return t;
    }

    static int premierMax(int[] t) {
        int b = 0;
        for (int i = 1; i < t.length; i++) if (t[i] > t[b]) b = i;
        return b;
    }

    static void testCompter(String s) {
        Check.test("compter(\"" + s + "\")", () -> {
            Object r = appeler("compter", s);
            Check.isTrue(r instanceof int[], "compter doit renvoyer un int[].");
            Check.isTrue(java.util.Arrays.equals(comptes(s), (int[]) r),
                "compter(\"" + s + "\") : attendu " + java.util.Arrays.toString(comptes(s)) + ", obtenu " + java.util.Arrays.toString((int[]) r));
        });
    }

    static void testIndex(int... t) {
        Check.test("indexMax de " + java.util.Arrays.toString(t), () -> {
            Check.equal(premierMax(t), appeler("indexMax", (Object) t.clone()), "indexMax(" + java.util.Arrays.toString(t) + ")");
        });
    }

    static void principal(String s) {
        Check.test("Inscription « " + s + " »", () -> {
            int[] t = comptes(s);
            int i = premierMax(t);
            var l = Check.lines(Check.runMain("Main", s + "\n"));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            if (t[i] == 0) {
                Check.equal("Le golem ne lit rien.", l.get(l.size() - 1), "Dernière ligne");
            } else {
                Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes (Lettre reine, Occurrences). Obtenu : " + l);
                Check.equal("Lettre reine : " + (char) ('a' + i), l.get(l.size() - 2), "Ligne Lettre reine");
                Check.equal("Occurrences : " + t[i], l.get(l.size() - 1), "Ligne Occurrences");
            }
        });
    }

    static void verifier() {
        for (String s : new String[]{"", "abc", "Aaa b!", "zz", "banane"}) testCompter(s);
        testIndex(0, 3, 3, 1);
        testIndex(5);
        testIndex(0, 0);
        testIndex(1, 2, 9, 9, 2);
        principal("banane");
        principal("abab");
        principal("ABC 123");
        principal("");
        principal("Zorro zozote");
    }
}
