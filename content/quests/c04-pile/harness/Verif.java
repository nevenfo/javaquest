
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
        Check.test("empile, opérations, sommet", () -> {
            Main.PILE.clear();
            Check.isTrue(Main.traiter("empile 10 3") == null, "empile est silencieuse : null attendu.");
            Check.isTrue(Main.traiter("moins") == null, "moins est silencieuse : null attendu.");
            Check.equal("7", Main.traiter("sommet"), "10 3 moins : a - b avec b au sommet");
            Main.traiter("empile 5");
            Main.traiter("fois");
            Check.equal("35", Main.traiter("sommet"), "7 5 fois");
            Check.equal("1", Main.traiter("taille"), "taille après les opérations");
        });
        Check.test("plus", () -> {
            Main.PILE.clear();
            Main.traiter("empile 2   3   4");
            Main.traiter("plus");
            Check.equal("7", Main.traiter("sommet"), "2 3 4 plus : 3 + 4 = 7");
            Check.equal("2", Main.traiter("taille"), "taille");
        });
        Check.test("pile insuffisante : pile inchangée", () -> {
            Main.PILE.clear();
            Main.traiter("empile 9");
            Check.equal("Erreur : pile insuffisante", Main.traiter("plus"), "plus avec un seul nombre");
            Check.equal("1", Main.traiter("taille"), "la pile ne doit pas avoir changé");
            Check.equal("9", Main.traiter("sommet"), "le sommet doit être toujours 9");
            Main.PILE.clear();
            Check.equal("Erreur : pile insuffisante", Main.traiter("fois"), "fois sur pile vide");
            Check.equal("vide", Main.traiter("sommet"), "sommet d'une pile vide");
        });
        Check.test("ligne blanche et commande inconnue", () -> {
            Check.isTrue(Main.traiter("  ") == null, "Ligne blanche : null attendu.");
            Check.equal("Commande inconnue : x", Main.traiter("x 1"), "commande inconnue");
        });
        Check.test("extensibilité : commande dup ajoutée par le harnais", () -> {
            Main.enregistrer("dup", args -> { Main.PILE.push(Main.PILE.peek()); return null; });
            Main.PILE.clear();
            Main.traiter("empile 4");
            Main.traiter("dup");
            Main.traiter("plus");
            Check.equal("8", Main.traiter("sommet"), "4 dup plus");
        });
        Check.test("boucle principale", () -> {
            Main.PILE.clear();
            Check.equal(List.of("42", "Erreur : pile insuffisante"), sortie("empile 6 7\nfois\nsommet\nplus\nfin\nsommet\n"), "scénario complet");
        });
    }
}
