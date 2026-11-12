import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Les champs sont privés", () -> {
            int n = 0;
            for (var f : Check.playerClass("Outil").getDeclaredFields()) {
                n++;
                Check.isTrue(java.lang.reflect.Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
            }
            Check.isTrue(n >= 2, "Il faut au moins deux champs (nom et durabilité).");
        });
        Check.test("Constructeur et accesseurs", () -> {
            Outil o = new Outil("Marteau", 3);
            Check.equal("Marteau", o.getNom(), "getNom()");
            Check.equal(3, o.getDurabilite(), "getDurabilite()");
            Outil p = new Outil("Lime", 10);
            Check.equal("Lime", p.getNom(), "Nom du second outil");
            Check.equal("Marteau", o.getNom(), "Nom du premier outil (les objets ne se partagent pas)");
        });
        Check.test("utiliser() retire 1", () -> {
            Outil o = new Outil("Pince", 3);
            o.utiliser();
            Check.equal(2, o.getDurabilite(), "Après un usage");
            o.utiliser();
            Check.equal(1, o.getDurabilite(), "Après deux usages");
        });
        Check.test("La durabilité reste positive", () -> {
            Outil o = new Outil("Scie", 1);
            o.utiliser(); o.utiliser(); o.utiliser();
            Check.equal(0, o.getDurabilite(), "Après trop d'usages");
            Outil z = new Outil("Cassé", 0);
            z.utiliser();
            Check.equal(0, z.getDurabilite(), "Outil déjà à zéro");
        });
    }
}
