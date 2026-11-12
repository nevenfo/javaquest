
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
        Check.test("ajoute puis montre", () -> {
            Check.equal(List.of("Total : 12"), sortie("ajoute 5\najoute 7\nmontre\nfin\n"), "ajoute 5, ajoute 7, montre");
        });
        Check.test("total initial", () -> {
            Check.equal(List.of("Total : 0"), sortie("montre\nfin\n"), "montre sans rien ajouter");
        });
        Check.test("négatifs, inconnue, arrêt à fin", () -> {
            Check.equal(List.of("Total : 3", "Total : -7", "Commande inconnue : saute"), sortie("ajoute 3\nmontre\najoute -10\nmontre\nsaute\nfin\najoute 100\nmontre\n"), "scénario avec fin au milieu de l'entrée");
        });
        Check.test("fin de l'entrée sans commande fin", () -> {
            Check.equal(List.of("Total : 2"), sortie("ajoute 2\nmontre\n"), "entrée qui se termine sans fin");
        });
        Check.test("espaces superflus", () -> {
            Check.equal(List.of("Total : 4"), sortie("  ajoute   4  \nmontre\nfin\n"), "espaces autour et entre les mots");
        });
    }
}
