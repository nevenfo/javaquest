
import fr.javaquest.runner.Check;

class Verif {
    static void essai(String titre, String[] args, String... attendu) {
        Check.test(titre, () -> {
            var l = Check.lines(Check.runMain("Main", "", args));
            Check.equal(java.util.List.of(attendu), l, "Lignes affichées");
        });
    }

    static void verifier() {
        essai("Aucun argument", new String[]{}, "Halte ! Donne ton nom.");
        essai("Un seul argument", new String[]{"Mira"}, "Bienvenue, Mira !");
        essai("Nom et durée", new String[]{"Mira", "3"}, "Bienvenue, Mira !", "Séjour : 72 heures");
        essai("Durée nulle", new String[]{"Tobias", "0"}, "Bienvenue, Tobias !", "Séjour : 0 heures");
        essai("Argument en trop ignoré", new String[]{"Ada", "10", "inutile"}, "Bienvenue, Ada !", "Séjour : 240 heures");
    }
}
