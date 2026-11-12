import java.util.*;
import java.util.function.IntBinaryOperator;

public class Main {
    static final Deque<Integer> PILE = new ArrayDeque<>();
    private static final Map<String, Commande> COMMANDES = new HashMap<>();

    static {
        enregistrer("empile", args -> { for (String a : args) PILE.push(Integer.parseInt(a)); return null; });
        enregistrer("plus", args -> operer(Integer::sum));
        enregistrer("moins", args -> operer((a, b) -> a - b));
        enregistrer("fois", args -> operer((a, b) -> a * b));
        enregistrer("sommet", args -> PILE.isEmpty() ? "vide" : String.valueOf(PILE.peek()));
        enregistrer("taille", args -> String.valueOf(PILE.size()));
    }

    private static String operer(IntBinaryOperator op) {
        int b = PILE.isEmpty() ? 0 : PILE.pop();
        if (PILE.isEmpty()) return "Erreur : pile insuffisante";
        int a = PILE.pop();
        PILE.push(op.applyAsInt(a, b));
        return null;
    }

    static void enregistrer(String nom, Commande c) { COMMANDES.put(nom, c); }

    static String traiter(String ligne) {
        if (ligne.isBlank()) return null;
        List<String> mots = new ArrayList<>(Arrays.asList(ligne.trim().split("\\s+")));
        String nom = mots.remove(0);
        Commande c = COMMANDES.get(nom);
        return c == null ? "Commande inconnue : " + nom : c.executer(mots);
    }

    public static void main(String[] args) {
        String ligne;
        while ((ligne = IO.readln()) != null && !ligne.equals("fin")) {
            String r = traiter(ligne);
            if (r != null) IO.println(r);
        }
    }
}
