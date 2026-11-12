
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static List<String> sortie(String in) throws Throwable {
        Set<String> echo = new HashSet<>(Check.lines(in));
        List<String> out = new ArrayList<>();
        for (String l : Check.lines(Check.runMain("Main", in))) if (!echo.contains(l)) out.add(l);
        return out;
    }
    static void verifier() {
        Check.test("somme", () -> {
            Check.equal("6", Main.traiter("somme 1 2 3"), "somme 1 2 3");
            Check.equal("0", Main.traiter("somme"), "somme sans argument");
            Check.equal("-4", Main.traiter("somme -5 1"), "somme avec un négatif");
        });
        Check.test("compte et echo", () -> {
            Check.equal("3", Main.traiter("compte a b c"), "compte a b c");
            Check.equal("0", Main.traiter("compte"), "compte sans argument");
            Check.equal("bonjour tout le monde", Main.traiter("echo bonjour tout le monde"), "echo");
        });
        Check.test("commande inconnue", () -> {
            Check.equal("Commande inconnue : foo", Main.traiter("foo 1 2"), "foo 1 2");
        });
        Check.test("extensibilité : ajouter une commande sans toucher traiter", () -> {
            Main.COMMANDES.put("double", args -> String.valueOf(2 * Integer.parseInt(args.get(0))));
            Check.equal("42", Main.traiter("double 21"), "commande ajoutée à la table : double 21");
        });
        Check.test("boucle principale", () -> {
            Check.equal(List.of("5", "3", "Commande inconnue : foo"), sortie("somme 2 3\ncompte a b c\nfoo\nfin\nsomme 1\n"), "scénario complet (arrêt à fin)");
        });
    }
}
