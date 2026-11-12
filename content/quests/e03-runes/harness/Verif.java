import fr.javaquest.runner.Check;
import java.util.List;

class Verif {
    static void cas(String nom, String entree, String doublon, String palindrome) {
        Check.test(nom, () -> {
            List<String> l = Check.lines(Check.runMain("Main", entree));
            Check.isTrue(l.size() >= 2, "Deux lignes de résultat attendues à la fin, obtenu : " + l);
            Check.equal("Doublon : " + doublon, l.get(l.size() - 2), "avant-dernière ligne");
            Check.equal("Plus long palindrome : " + palindrome, l.get(l.size() - 1), "dernière ligne");
        });
    }

    static void verifier() {
        cas("Exemple de l'énoncé", "kayak\nrune\nlame\nrotor\nrune\naaaaaaa\nFIN\n", "rune", "kayak");
        cas("Égalité de longueur : le premier lu", "elle\nvent\nanna\nfeu\nvent\nFIN\n", "vent", "elle");
        cas("Aucun palindrome valable", "zzzz\nmot\nbrume\nmot\nFIN\n", "mot", "aucun");
        cas("Le doublon arrive en dernier", "radar\nressasser\nsel\npierre\nsel\nFIN\n", "sel", "ressasser");
        cas("Une lettre seule n'est pas comptée", "x\nrotor\nstats\nrotor\nFIN\n", "rotor", "rotor");
    }
}
