
import fr.javaquest.runner.Check;
import java.lang.reflect.Method;
import java.util.*;

class Verif {
    static List<String> noms(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) l.add(m.getName());
        return l;
    }
    static void verifier() {
        Check.test("Guerrier : combattant d'attaque 8", () -> {
            Combattant c = new Guerrier();
            Check.equal(8, c.attaque(), "attaque du guerrier");
            Check.equal("À l'attaque (8) !", c.cri(), "cri() hérité de l'interface");
            Check.isTrue(!(((Object) c) instanceof Soigneur), "Un guerrier n'est pas un soigneur.");
        });
        Check.test("Prêtre : soigneur de soin 6", () -> {
            Soigneur s = new Pretre();
            Check.equal(6, s.soin(), "soin du prêtre");
            Check.equal("Je soigne 6 PV.", s.priere(), "priere() héritée");
            Check.isTrue(!(((Object) s) instanceof Combattant), "Un prêtre ne doit pas être un combattant.");
        });
        Check.test("Paladin : les deux capacités", () -> {
            Paladin p = new Paladin();
            Combattant c = p;
            Soigneur s = p;
            Check.equal(5, c.attaque(), "attaque du paladin");
            Check.equal(4, s.soin(), "soin du paladin");
            Check.equal("À l'attaque (5) !", c.cri(), "cri() du paladin");
            Check.equal("Je soigne 4 PV.", s.priere(), "priere() du paladin");
        });
        Check.test("Pas de duplication des méthodes default", () -> {
            for (Class<?> k : List.of(Guerrier.class, Pretre.class, Paladin.class)) {
                Check.isTrue(!noms(k).contains("cri") && !noms(k).contains("priere"), k.getSimpleName() + " redéfinit cri() ou priere() : laisse l'interface les fournir.");
            }
        });
    }
}
