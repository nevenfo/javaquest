import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void essai(String titre, List<String> entree, String... attendu) {
        Check.test(titre, () -> {
            StringBuilder in = new StringBuilder();
            for (String s : entree) in.append(s).append("\n");
            in.append("fin\n");
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= attendu.length, "Il manque des lignes : attendu " + attendu.length + " ligne(s) de résultat, obtenu : " + l);
            for (int i = 0; i < attendu.length; i++) {
                Check.equal(attendu[i], l.get(l.size() - attendu.length + i), "Ligne " + (i + 1) + " du résultat");
            }
        });
    }

    static void verifier() {
        essai("Cumul de deux fournitures", List.of("pomme;3", "poire;5", "pomme;4"), "pomme : 7", "poire : 5");
        essai("Rien en stock", List.of(), "Stock vide");
        essai("Égalités : ordre alphabétique", List.of("zinc;2", "acier;2", "bois;5", "cuivre;2"), "bois : 5", "acier : 2", "cuivre : 2", "zinc : 2");
        essai("Espaces autour des champs", List.of(" lampe ; 2 ", "lampe;-1", "huile ;1"), "huile : 1", "lampe : 1");
        essai("Total nul conservé", List.of("x;3", "x;-3", "y;1"), "y : 1", "x : 0");
        essai("Une seule ligne", List.of("sel;9"), "sel : 9");
    }
}
