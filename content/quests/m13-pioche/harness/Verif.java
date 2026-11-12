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
        int[][] d = {{4, 3, 10}, {0, 0, 0}, {10, 5, 20}, {1, 8, 17}};
        for (int[] c : d) {
            Check.test("degats(" + c[0] + ", " + c[1] + ")", () -> {
                Object r = appeler("degats", c[0], c[1]);
                Check.equal(c[2], r, "Valeur retournée par degats(" + c[0] + ", " + c[1] + ")");
            });
        }
        int[] duretes = {7, 6, 12, 0, 1};
        for (int du : duretes) {
            Check.test("estSolide(" + du + ")", () -> {
                Object r = appeler("estSolide", du);
                Check.equal(du >= 7, r, "Valeur retournée par estSolide(" + du + ")");
            });
        }
    }
}
