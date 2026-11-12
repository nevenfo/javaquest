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

    static boolean attendu(String s) {
        try { Integer.parseInt(s); return true; } catch (NumberFormatException e) { return false; }
    }

    static void principal(String... lignes) {
        Check.test("Relevés " + java.util.Arrays.toString(lignes), () -> {
            int v = 0, somme = 0;
            StringBuilder in = new StringBuilder(lignes.length + "\n");
            for (String s : lignes) { in.append(s).append("\n"); if (attendu(s)) { v++; somme += Integer.parseInt(s); } }
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes (Valides, Somme). Obtenu : " + l);
            Check.equal("Valides : " + v, l.get(l.size() - 2), "Ligne Valides");
            Check.equal("Somme : " + somme, l.get(l.size() - 1), "Ligne Somme");
        });
    }

    static void verifier() {
        for (String s : new String[]{"12", "-4", "0", "abc", "", "3.5", " 7", "+5", "2147483648", "9x"}) {
            Check.test("estValide(\"" + s + "\")", () -> Check.equal(attendu(s), appeler("estValide", s), "estValide(\"" + s + "\")"));
        }
        principal("12", "-4", "abc", "7");
        principal("5");
        principal("pierre", "feu", "eau");
        principal("-1", "2x", "-3", "3.5", "0");
        principal("40", "", "2");
    }
}
