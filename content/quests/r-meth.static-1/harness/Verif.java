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
        int[] xps = {0, 99, 100, 499, 500, 5000};
        for (int xp : xps) {
            Check.test("grade(" + xp + ")", () -> {
                String voulu = xp < 100 ? "Novice" : xp < 500 ? "Adepte" : "Maître";
                Check.equal(voulu, appeler("grade", xp), "grade(" + xp + ")");
            });
        }
        int[][] ab = {{3, 10}, {10, 3}, {5, 5}, {-4, 6}, {-8, -2}};
        for (int[] c : ab) {
            Check.test("ecart(" + c[0] + ", " + c[1] + ")", () -> {
                Check.equal(Math.abs(c[0] - c[1]), appeler("ecart", c[0], c[1]), "ecart(" + c[0] + ", " + c[1] + ")");
            });
        }
    }
}
