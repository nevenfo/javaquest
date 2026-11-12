
import fr.javaquest.runner.Check;
import java.lang.reflect.Method;
import java.util.*;

class Verif {
    static boolean declare(Class<?> c, String nom) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(nom)) return true;
        return false;
    }
    static void verifier() {
        Check.test("Interfaces avec méthode default", () -> {
            Check.isTrue(Roulant.class.isInterface() && Flottant.class.isInterface(), "Roulant et Flottant doivent être des interfaces.");
            Check.isTrue(Roulant.class.getMethod("deplacement").isDefault(), "Roulant.deplacement() doit être default.");
            Check.isTrue(Flottant.class.getMethod("deplacement").isDefault(), "Flottant.deplacement() doit être default.");
        });
        Check.test("Charrette et barque", () -> {
            Roulant c = new Charrette();
            Check.equal(10, c.vitesseRoute(), "vitesse de la charrette");
            Check.equal("roule à 10 km/h", c.deplacement(), "Charrette.deplacement()");
            Flottant b = new Barque();
            Check.equal(4, b.vitesseEau(), "vitesse de la barque");
            Check.equal("flotte à 4 km/h", b.deplacement(), "Barque.deplacement()");
            Check.isTrue(!((Object) c instanceof Flottant) && !((Object) b instanceof Roulant), "Charrette : seulement roulante ; Barque : seulement flottante.");
        });
        Check.test("Amphibie : deux capacités", () -> {
            Amphibie a = new Amphibie();
            Roulant r = a;
            Flottant f = a;
            Check.equal(30, r.vitesseRoute(), "vitesse sur route");
            Check.equal(8, f.vitesseEau(), "vitesse sur l'eau");
            Check.equal("roule à 30 km/h et flotte à 8 km/h", a.deplacement(), "Amphibie.deplacement()");
            Check.equal("roule à 30 km/h et flotte à 8 km/h", r.deplacement(), "deplacement() via Roulant (polymorphisme)");
        });
        Check.test("Pas de duplication", () -> {
            Check.isTrue(!declare(Charrette.class, "deplacement"), "Charrette ne doit pas redéfinir deplacement().");
            Check.isTrue(!declare(Barque.class, "deplacement"), "Barque ne doit pas redéfinir deplacement().");
        });
    }
}
