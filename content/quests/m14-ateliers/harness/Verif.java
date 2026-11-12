import fr.javaquest.runner.Check;

class Verif {
    static Object appeler(String nom, Object... args) throws Throwable {
        Class<?> c = Check.playerClass("Main");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(nom) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                Object cible = null;
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                    var k = c.getDeclaredConstructor();
                    k.setAccessible(true);
                    cible = k.newInstance();
                }
                try {
                    return m.invoke(cible, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }
        Check.fail("La méthode " + nom + " (avec " + args.length + " paramètre(s)) est introuvable dans Main.");
        return null;
    }

    static void verifier() {
        int[][] p = {{2, 0}, {2, 10}, {3, 5}, {0, 5}, {-2, 3}, {10, 18}, {2, 40}};
        for (int[] c : p) {
            Check.test("puissance(" + c[0] + ", " + c[1] + ")", () -> {
                long voulu = 1;
                for (int i = 0; i < c[1]; i++) voulu *= c[0];
                Object r = appeler("puissance", c[0], c[1]);
                Check.isTrue(r instanceof Number, "puissance doit retourner un nombre.");
                Check.equal(voulu, ((Number) r).longValue(), "puissance(" + c[0] + ", " + c[1] + ")");
            });
        }
        int[] n = {-3, 0, 1, 2, 3, 4, 9, 17, 25, 97, 100};
        for (int x : n) {
            Check.test("estPremier(" + x + ")", () -> {
                boolean voulu = x >= 2;
                for (int d = 2; d * d <= x; d++) if (x % d == 0) voulu = false;
                Check.equal(voulu, appeler("estPremier", x), "estPremier(" + x + ")");
            });
        }
        Object[][] o = {{"banane", 'a', 2}, {"banane", 'z', 0}, {"", 'a', 0}, {"aaaa", 'a', 4}, {"Aa", 'a', 1}, {"javelune", 'e', 2}};
        for (Object[] c : o) {
            Check.test("occurrences(\"" + c[0] + "\", '" + c[1] + "')", () -> {
                Check.equal(c[2], appeler("occurrences", c[0], c[1]), "occurrences(\"" + c[0] + "\", '" + c[1] + "')");
            });
        }
    }
}
