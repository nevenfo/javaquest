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

    static String joindre(int[] v) {
        int[] t = v.clone();
        java.util.Arrays.sort(t);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.length; i++) sb.append(i == 0 ? "" : " ").append(t[i]);
        return sb.toString();
    }

    static void principal(String mot, int... v) {
        Check.test("Mot « " + mot + " », gemmes " + java.util.Arrays.toString(v), () -> {
            StringBuilder in = new StringBuilder(mot + "\n" + v.length + "\n");
            int somme = 0, max = v[0];
            for (int x : v) { in.append(x).append("\n"); somme += x; max = Math.max(max, x); }
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= 3, "Il faut afficher trois lignes (Triées, Total, Maximum). Obtenu : " + l);
            Check.equal("Triées : " + joindre(v), l.get(l.size() - 3), "Ligne Triées");
            Check.equal("Total : " + somme, l.get(l.size() - 2), "Ligne Total");
            Check.equal("Maximum : " + max, l.get(l.size() - 1), "Ligne Maximum");
        });
    }

    static void muet(String mot) {
        Check.test("Mot « " + mot + " » refusé", () -> {
            var l = Check.lines(Check.runMain("Main", mot + "\n3\n9\n1\n4\n"));
            Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
            Check.equal("Le spectre reste muet.", l.get(l.size() - 1), "Dernière ligne");
            Check.isTrue(l.stream().noneMatch(s -> s.startsWith("Total")), "Le spectre ne doit rien calculer pour un mot qui n'est pas un miroir.");
        });
    }

    static void verifier() {
        for (String s : new String[]{"radar", "abba", "x", "abca", "ab", "Kayak"}) {
            Check.test("estMiroir(\"" + s + "\")", () -> {
                boolean voulu = new StringBuilder(s).reverse().toString().equals(s);
                Check.equal(voulu, appeler("estMiroir", s), "estMiroir(\"" + s + "\")");
            });
        }
        int[][] tabs = {{1, 2, 3}, {-1, 1}, {7}, {-5, -2, -9}, {4, 4, 4, 5}};
        for (int[] t : tabs) {
            Check.test("total et maximum de " + java.util.Arrays.toString(t), () -> {
                int s = 0, m = t[0];
                for (int x : t) { s += x; m = Math.max(m, x); }
                int[] copie = t.clone();
                Check.equal(s, appeler("total", (Object) copie), "total(" + java.util.Arrays.toString(t) + ")");
                Check.equal(m, appeler("maximum", (Object) copie), "maximum(" + java.util.Arrays.toString(t) + ")");
            });
        }
        principal("radar", 9, 1, 4);
        principal("x", 5);
        principal("ressasser", -2, -8, -5, 0);
        principal("abba", 3, 3, 3);
        muet("grimoire");
        muet("ab");
    }
}
