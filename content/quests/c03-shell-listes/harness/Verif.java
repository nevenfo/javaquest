
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
        Check.test("Structure : interface Commande", () -> {
            Check.isTrue(Check.playerClass("Commande").isInterface(), "Commande doit être une interface.");
        });
        Check.test("ajoute, taille, montre", () -> {
            Main.reinitialiser();
            Check.isTrue(Main.traiter("ajoute a b c") == null, "ajoute est silencieuse : null attendu.");
            Check.equal("3", Main.traiter("taille"), "taille");
            Check.equal("[a, b, c]", Main.traiter("montre"), "montre");
        });
        Check.test("retire et inverse", () -> {
            Main.reinitialiser();
            Main.traiter("ajoute a b c b");
            Check.equal("retiré", Main.traiter("retire b"), "retire b (présent)");
            Check.equal("absent", Main.traiter("retire z"), "retire z (absent)");
            Check.equal("[a, c, b]", Main.traiter("montre"), "seule la première occurrence est retirée");
            Check.isTrue(Main.traiter("inverse") == null, "inverse est silencieuse.");
            Check.equal("[b, c, a]", Main.traiter("montre"), "après inverse");
        });
        Check.test("blancs multiples, ligne vide, inconnue", () -> {
            Main.reinitialiser();
            Main.traiter("ajoute   a    b");
            Check.equal("2", Main.traiter("  taille  "), "taille après ajoute avec blancs multiples");
            Check.isTrue(Main.traiter("   ") == null, "Une ligne blanche ne produit rien : null attendu.");
            Check.equal("Commande inconnue : xyz", Main.traiter("xyz 1"), "commande inconnue");
            Main.reinitialiser();
            Check.equal("0", Main.traiter("taille"), "taille après reinitialiser");
            Check.equal("[]", Main.traiter("montre"), "montre d'une liste vide");
        });
        Check.test("extensibilité : enregistrer une nouvelle commande", () -> {
            Main.enregistrer("compte-lettres", args -> String.valueOf(args.stream().mapToInt(String::length).sum()));
            Check.equal("5", Main.traiter("compte-lettres ab cde"), "commande ajoutée par le harnais");
            Check.equal("Commande inconnue : nouvelle", Main.traiter("nouvelle"), "une commande non enregistrée reste inconnue");
        });
        Check.test("boucle principale", () -> {
            Main.reinitialiser();
            Check.equal(List.of("[x, y]", "Commande inconnue : saute"), sortie("ajoute x y\nmontre\nsaute\nfin\nmontre\n"), "main lit jusqu'à fin");
            Main.reinitialiser();
            Check.equal(List.of("1"), sortie("ajoute z\ntaille\n"), "main s'arrête aussi à la fin de l'entrée");
        });
    }
}
