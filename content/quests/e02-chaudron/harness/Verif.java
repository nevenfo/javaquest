import fr.javaquest.runner.Check;

class Verif {
    static void leve(Class<? extends Throwable> attendu, Runnable r, String quoi) {
        try { r.run(); } catch (Throwable t) {
            Check.isTrue(attendu.isInstance(t), quoi + " : " + attendu.getSimpleName() + " attendue, " + t.getClass().getSimpleName() + " obtenue");
            return;
        }
        Check.fail(quoi + " : une " + attendu.getSimpleName() + " devait être levée");
    }

    static void verifier() {
        Check.test("Capacité invalide refusée", () -> {
            leve(IllegalArgumentException.class, () -> new Chaudron(0), "new Chaudron(0)");
            leve(IllegalArgumentException.class, () -> new Chaudron(-5), "new Chaudron(-5)");
        });
        Check.test("Verser accumule les doses", () -> {
            Chaudron c = new Chaudron(10);
            Check.equal(0, c.contenu(), "contenu initial");
            c.verser("ortie", 3); c.verser("menthe", 2); c.verser("ortie", 1);
            Check.equal(6, c.contenu(), "contenu après 3 versements");
            Check.equal(4, c.doses("ortie"), "doses d'ortie");
            Check.equal(0, c.doses("sauge"), "doses d'un ingrédient absent");
        });
        Check.test("Doses invalides et débordement refusés", () -> {
            Chaudron c = new Chaudron(5);
            leve(IllegalArgumentException.class, () -> c.verser("ortie", 0), "verser 0 dose");
            leve(IllegalArgumentException.class, () -> c.verser("ortie", -2), "verser -2 doses");
            c.verser("ortie", 4);
            leve(IllegalStateException.class, () -> c.verser("menthe", 2), "dépasser la capacité");
            Check.equal(4, c.contenu(), "le versement refusé ne doit rien changer");
            c.verser("menthe", 1);
            Check.equal(5, c.contenu(), "remplissage exact à la capacité");
        });
        Check.test("Préparer : ingrédient majoritaire, égalité → premier versé, puis vidage", () -> {
            Chaudron c = new Chaudron(20);
            c.verser("sauge", 2); c.verser("ortie", 5); c.verser("menthe", 3);
            Check.equal("Potion de ortie", c.preparer(), "préparation");
            Check.equal(0, c.contenu(), "contenu après préparation");
            Check.equal(0, c.doses("ortie"), "ortie après préparation");
            c.verser("menthe", 2); c.verser("sauge", 2);
            Check.equal("Potion de menthe", c.preparer(), "égalité de doses");
        });
        Check.test("Préparer un chaudron vide est refusé", () -> {
            Chaudron c = new Chaudron(3);
            leve(IllegalStateException.class, c::preparer, "preparer() sur un chaudron vide");
        });
        Check.test("Encapsulation : aucun champ public", () -> {
            for (var f : Chaudron.class.getDeclaredFields())
                Check.isTrue(!java.lang.reflect.Modifier.isPublic(f.getModifiers()), "le champ " + f.getName() + " ne doit pas être public");
        });
    }
}
