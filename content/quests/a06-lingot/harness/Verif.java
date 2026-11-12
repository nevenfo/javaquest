
import fr.javaquest.runner.Check;
import java.lang.reflect.*;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Champs privés et accesseurs", () -> {
            for (Field f : Check.playerClass("Lingot").getDeclaredFields())
                if (!Modifier.isStatic(f.getModifiers())) {
                    Check.isTrue(Modifier.isPrivate(f.getModifiers()), "Le champ " + f.getName() + " doit être private.");
                    Check.isTrue(Modifier.isFinal(f.getModifiers()), "Le champ " + f.getName() + " doit être final.");
                }
            Lingot l = new Lingot("argent", 7);
            Check.equal("argent", l.getMetal(), "getMetal()");
            Check.equal(7, l.getPoids(), "getPoids()");
        });
        Check.test("Égalité par contenu (métal construit à part)", () -> {
            Object a = new Lingot(new String("or"), 12), b = new Lingot(new String("or"), 12);
            Check.isTrue(a.equals(b), "Deux lingots d'or de poids 12 doivent être égaux (compare les métaux avec equals, pas ==).");
            Check.isTrue(!a.equals(new Lingot("or", 13)), "Poids différents : lingots différents.");
            Check.isTrue(!a.equals(new Lingot("fer", 12)), "Métaux différents : lingots différents.");
        });
        Check.test("equals sans accident", () -> {
            Object a = new Lingot("or", 1);
            Check.isTrue(!a.equals(null), "equals(null) doit répondre false.");
            Check.isTrue(!a.equals("or"), "Un lingot n'est pas une chaîne.");
        });
        Check.test("hashCode cohérent", () -> {
            Check.equal(new Lingot(new String("cuivre"), 3).hashCode(), new Lingot(new String("cuivre"), 3).hashCode(), "hashCode de deux lingots égaux");
        });
        Check.test("Réserve (HashSet) sans doublon", () -> {
            Set<Lingot> reserve = new HashSet<>();
            for (String m : new String[]{"or", "or", "fer", "or", "fer"}) reserve.add(new Lingot(new String(m), m.length()));
            Check.equal(2, reserve.size(), "Nombre de lingots distincts");
        });
        Check.test("toString", () -> {
            Check.equal("Lingot(or, 12)", new Lingot("or", 12).toString(), "toString()");
            Check.equal("Lingot(fer, 0)", new Lingot("fer", 0).toString(), "toString() d'un lingot de poids 0");
        });
    }
}
