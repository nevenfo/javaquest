import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("Les champs sont privés", () -> {
            for (var f : Check.playerClass("Tiroir").getDeclaredFields())
                Check.isTrue(java.lang.reflect.Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
        });
        Check.test("Tiroir neuf", () -> {
            Tiroir t = new Tiroir(10);
            Check.equal(10, t.getCapacite(), "getCapacite()");
            Check.equal(0, t.getContenu(), "getContenu()");
            Check.isTrue(t.estVide(), "Un tiroir neuf est vide.");
            Check.isTrue(!t.estPlein(), "Un tiroir neuf n'est pas plein.");
        });
        Check.test("ranger", () -> {
            Tiroir t = new Tiroir(10);
            Check.isTrue(t.ranger(8), "Ranger 8 dans 10 doit réussir.");
            Check.equal(8, t.getContenu(), "Contenu après 8");
            Check.isTrue(!t.ranger(5), "Ranger 5 de plus doit échouer.");
            Check.equal(8, t.getContenu(), "Un refus ne change rien");
            Check.isTrue(t.ranger(2), "Ranger 2 pour arriver pile à la capacité doit réussir.");
            Check.isTrue(t.estPlein(), "Tiroir plein.");
            Check.isTrue(!t.ranger(0) && !t.ranger(-3), "Ranger 0 ou un nombre négatif doit échouer.");
            Check.equal(10, t.getContenu(), "Contenu final");
        });
        Check.test("sortir", () -> {
            Tiroir t = new Tiroir(10);
            t.ranger(6);
            Check.isTrue(t.sortir(4), "Sortir 4 de 6 doit réussir.");
            Check.equal(2, t.getContenu(), "Contenu après sortie");
            Check.isTrue(!t.sortir(3), "Sortir 3 de 2 doit échouer.");
            Check.equal(2, t.getContenu(), "Un refus ne change rien");
            Check.isTrue(!t.sortir(0) && !t.sortir(-1), "Sortir 0 ou un nombre négatif doit échouer.");
            Check.isTrue(t.sortir(2), "Sortir tout doit réussir.");
            Check.isTrue(t.estVide(), "Tiroir vide.");
        });
        Check.test("Deux tiroirs indépendants", () -> {
            Tiroir a = new Tiroir(5), b = new Tiroir(50);
            a.ranger(5);
            Check.equal(0, b.getContenu(), "Le second tiroir n'est pas touché");
            Check.isTrue(b.ranger(30), "Le second tiroir a sa propre capacité.");
        });
    }
}
