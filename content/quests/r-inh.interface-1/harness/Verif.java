
import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Lanceur est une interface", () -> Check.isTrue(Check.playerClass("Lanceur").isInterface(), "Lanceur doit être une interface."));
        String[] cibles = {"orc", "", "dragon de feu"};
        for (String c : cibles)
            Check.test("Lanceurs sur la cible « " + c + " »", () -> {
                Lanceur[] l = {new Archer(), new Mage(), cible2 -> "Pierre sur " + cible2};
                Check.equal("Flèche sur " + c, l[0].lancer(c), "Archer");
                Check.equal("Boule de feu sur " + c, l[1].lancer(c), "Mage");
                Check.equal("Pierre sur " + c, l[2].lancer(c), "Lanceur du harnais");
            });
        Check.test("Les classes implémentent l'interface", () -> {
            Check.isTrue(Lanceur.class.isAssignableFrom(Check.playerClass("Archer")) && Lanceur.class.isAssignableFrom(Check.playerClass("Mage")), "Archer et Mage doivent déclarer `implements Lanceur`.");
        });
    }
}
