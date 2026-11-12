import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("C'est un record à deux composants", () -> {
            Class<?> c = Check.playerClass("Ordonnance");
            Check.isTrue(c.isRecord(), "Ordonnance doit être un record.");
            var comp = c.getRecordComponents();
            Check.equal(2, comp.length, "Nombre de composants");
            Check.equal("potion", comp[0].getName(), "Premier composant");
            Check.equal("doses", comp[1].getName(), "Second composant");
        });
        Check.test("Accesseurs, equals et toString fournis", () -> {
            Ordonnance o = new Ordonnance("Feu", 2);
            Check.equal("Feu", o.potion(), "potion()");
            Check.equal(2, o.doses(), "doses()");
            Check.equal(new Ordonnance("Feu", 2), o, "Deux ordonnances identiques");
            Check.equal("Ordonnance[potion=Feu, doses=2]", o.toString(), "toString()");
        });
        Check.test("Doses invalides", () -> {
            for (int d : new int[]{0, -1, -50}) {
                boolean leve = false;
                try { new Ordonnance("Feu", d); } catch (IllegalArgumentException e) { leve = true; }
                Check.isTrue(leve, "doses = " + d + " doit lever IllegalArgumentException.");
            }
        });
        Check.test("Potion invalide", () -> {
            for (String p : new String[]{null, "", "   "}) {
                boolean leve = false;
                try { new Ordonnance(p, 1); } catch (IllegalArgumentException e) { leve = true; }
                Check.isTrue(leve, "potion = " + (p == null ? "null" : "\"" + p + "\"") + " doit lever IllegalArgumentException.");
            }
        });
        Check.test("doubler()", () -> {
            Ordonnance o = new Ordonnance("Eau", 3);
            Check.equal(new Ordonnance("Eau", 6), o.doubler(), "Résultat de doubler()");
            Check.equal(3, o.doses(), "L'original ne change pas (un record est immuable)");
        });
    }
}
