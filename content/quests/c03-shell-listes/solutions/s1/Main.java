import java.util.*;

public class Main {
    private static final List<String> LISTE = new ArrayList<>();
    private static final Map<String, Commande> COMMANDES = new HashMap<>();

    static {
        enregistrer("ajoute", args -> { LISTE.addAll(args); return null; });
        enregistrer("taille", args -> String.valueOf(LISTE.size()));
        enregistrer("montre", args -> LISTE.toString());
        enregistrer("retire", args -> LISTE.remove(args.get(0)) ? "retiré" : "absent");
        enregistrer("inverse", args -> { Collections.reverse(LISTE); return null; });
    }

    static void enregistrer(String nom, Commande c) { COMMANDES.put(nom, c); }

    static void reinitialiser() { LISTE.clear(); }

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
