import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void index(String titre, List<String> fiches, Object... paires) {
        Check.test(titre, () -> {
            Map<String, List<String>> attendu = new TreeMap<>();
            for (int i = 0; i < paires.length; i += 2) attendu.put((String) paires[i], (List<String>) paires[i + 1]);
            Map<String, List<String>> obtenu = Main.indexer(fiches);
            Check.isTrue(obtenu != null, "indexer a renvoyé null.");
            Check.equal(attendu, obtenu, "index de " + fiches);
            Check.equal(new ArrayList<>(attendu.keySet()), new ArrayList<>(obtenu.keySet()), "ordre des étiquettes (alphabétique) pour " + fiches);
        });
    }

    static Map<String, List<String>> carte(Object... paires) {
        Map<String, List<String>> m = new HashMap<>();
        for (int i = 0; i < paires.length; i += 2) m.put((String) paires[i], (List<String>) paires[i + 1]);
        return m;
    }

    static void classe(String titre, Map<String, List<String>> index, String... attendu) {
        Check.test(titre, () -> {
            Map<String, List<String>> copie = new HashMap<>(index);
            Check.equal(List.of(attendu), Main.classement(index), "classement de " + copie.keySet());
            Check.equal(copie, index, "classement ne doit pas modifier l'index reçu");
        });
    }

    static void verifier() {
        index("indexer : deux fiches partagent une étiquette",
            List.of("Dragons;magie,feu", "Brasier;feu", "Sortilèges;Magie, Eau"),
            "eau", List.of("Sortilèges"), "feu", List.of("Brasier", "Dragons"), "magie", List.of("Dragons", "Sortilèges"));
        index("indexer : aucune fiche", List.of());
        index("indexer : fiche sans étiquette, ligne vide, virgules en trop",
            List.of("Seul;", "", "A;x,,x", "  "), "x", List.of("A"));
        index("indexer : même titre répété, un seul rattachement par étiquette",
            List.of("Atlas;cartes", "Atlas;cartes", "Atlas;cartes,mer"), "cartes", List.of("Atlas"), "mer", List.of("Atlas"));
        index("indexer : espaces et majuscules ignorés",
            List.of(" Zèbre ; Rayures , RAYURES ", "Abeille;rayures"), "rayures", List.of("Abeille", "Zèbre"));
        classe("classement : plus de titres d'abord, puis ordre alphabétique",
            carte("feu", List.of("a", "b"), "magie", List.of("c", "d"), "eau", List.of("e"), "air", List.of("f", "g", "h"), "zen", List.of("i")),
            "air (3)", "feu (2)", "magie (2)", "eau (1)", "zen (1)");
        classe("classement : index vide", carte());
        classe("classement : une seule étiquette", carte("mer", List.of("Atlas")), "mer (1)");
        classe("classement : tout à égalité", carte("c", List.of("x"), "a", List.of("y"), "b", List.of("z")), "a (1)", "b (1)", "c (1)");
    }
}
