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
        Check.test("avance n : n pas dans la direction du cap", () -> {
            Compagnon c = new Compagnon("C.....", "......");
            Check.equal("ok", Main.traiter(c, "avance 3"), "réponse de « avance 3 »");
            Check.equal(3, c.x(), "colonne après avance 3");
            Check.equal("ok", Main.traiter(c, "avance 0"), "réponse de « avance 0 »");
            Check.equal(3, c.x(), "avance 0 ne bouge pas");
        });
        Check.test("tourne : quart de tour à droite, puis avance", () -> {
            Compagnon c = new Compagnon("C...", "....", "....", "....");
            Check.equal("ok", Main.traiter(c, "tourne"), "réponse de « tourne »");
            Check.equal("bas", c.cap(), "cap après un quart de tour");
            Main.traiter(c, "avance 2");
            Check.equal(2, c.y(), "ligne après tourne puis avance 2");
            Check.equal(0, c.x(), "colonne inchangée");
            for (int i = 0; i < 3; i++) Main.traiter(c, "tourne");
            Check.equal("droite", c.cap(), "cap après trois virages de plus (bas, gauche, haut, droite)");
        });
        Check.test("dis : le texte entier, blancs normalisés", () -> {
            Compagnon c = new Compagnon("C...");
            Check.equal("ok", Main.traiter(c, "dis bonjour tout le monde"), "réponse de « dis … »");
            Main.traiter(c, "  dis   a    b  ");
            Check.equal(List.of("bonjour tout le monde", "a b"), c.dits(), "phrases dites");
            Check.equal("Erreur : rien à dire", Main.traiter(c, "dis"), "dis sans texte");
            Check.equal(2, c.dits().size(), "dis sans texte ne dit rien");
        });
        Check.test("arguments invalides de avance : exception gérée, automate immobile", () -> {
            Compagnon c = new Compagnon("C.....");
            for (String l : new String[]{"avance", "avance x", "avance -2", "avance 1.5", "avance 1 2"})
                Check.equal("Erreur : nombre attendu", Main.traiter(c, l), "réponse de « " + l + " »");
            Check.equal(0, c.x(), "aucun pas ne doit avoir été fait");
        });
        Check.test("mur : message d'erreur, le REPL continue", () -> {
            Compagnon c = new Compagnon("C.#...", "......");
            String r = Main.traiter(c, "avance 5");
            Check.isTrue(r != null && r.startsWith("Erreur : ") && r.length() > 9, "Un mur sur le chemin doit donner « Erreur : <message> » (reçu : " + r + ").");
            Check.equal(1, c.x(), "l'automate s'arrête devant le mur");
            Check.equal("ok", Main.traiter(c, "tourne"), "le REPL reste utilisable après l'erreur");
        });
        Check.test("commande inconnue, ligne vide, blancs multiples", () -> {
            Compagnon c = new Compagnon("C......");
            Check.equal("Commande inconnue : saute", Main.traiter(c, "saute 2"), "commande inconnue");
            Check.isTrue(Main.traiter(c, "   ") == null, "Une ligne blanche ne produit rien : null attendu.");
            Check.isTrue(Main.traiter(c, "") == null, "Une ligne vide ne produit rien : null attendu.");
            Check.equal("ok", Main.traiter(c, "  avance   2  "), "blancs multiples");
            Check.equal(2, c.x(), "colonne");
        });
        Check.test("extensibilité : enregistrer une nouvelle commande", () -> {
            Main.enregistrer("bond", (c, args) -> { c.avancer(); c.avancer(); c.dire("hop " + args.size()); });
            Compagnon c = new Compagnon("C....");
            Check.equal("ok", Main.traiter(c, "bond a b"), "commande ajoutée par le harnais");
            Check.equal(2, c.x(), "elle a bien été exécutée");
            Check.equal(List.of("hop 2"), c.dits(), "arguments transmis");
            Main.enregistrer("rate", (c2, args) -> { throw new CommandeException("raté"); });
            Check.equal("Erreur : raté", Main.traiter(c, "rate"), "CommandeException d'une commande ajoutée");
        });
        Check.test("boucle principale : lit jusqu'à fin", () -> {
            Check.equal(List.of("ok", "ok", "Erreur : nombre attendu", "Commande inconnue : saute", "ok"),
                sortie("avance 2\ntourne\navance x\nsaute\n\ndis salut\nfin\navance 1\n"), "main s'arrête à fin");
            Check.equal(List.of("ok"), sortie("tourne\n"), "main s'arrête aussi à la fin de l'entrée");
        });
    }
}
