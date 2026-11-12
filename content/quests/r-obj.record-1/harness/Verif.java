
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Potion est un record", () -> Check.isTrue(Check.playerClass("Potion").isRecord(), "Potion doit rester un record."));
        Check.test("dose négative refusée", () -> {
            boolean leve = false;
            try { new Potion("feu", -1); } catch (IllegalArgumentException e) { leve = true; }
            Check.isTrue(leve, "dose < 0 doit lever IllegalArgumentException.");
        });
        Check.test("dose 0 acceptée", () -> Check.equal(0, new Potion("eau", 0).dose(), "dose"));
        int[][] cas = {{10, 5}, {7, 3}, {1, 0}, {0, 0}};
        for (int[] c : cas)
            Check.test("diluer une dose de " + c[0], () -> {
                Potion p = new Potion("mana", c[0]);
                Check.equal(new Potion("mana", c[1]), p.diluer(), "diluer()");
                Check.equal(c[0], p.dose(), "dose d'origine");
            });
    }
}
