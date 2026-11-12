import java.util.*;

public class Main {
    private static final List<String> LISTE = new ArrayList<>();
    private static final Map<String, Commande> COMMANDES = new HashMap<>();

    static void enregistrer(String nom, Commande c) { COMMANDES.put(nom, c); }

    static void reinitialiser() { LISTE.clear(); }

    static String traiter(String ligne) {
        if (ligne.isBlank()) return null;
        List<String> mots = new ArrayList<>(Arrays.asList(ligne.trim().split("\\s+")));
        String nom = mots.remove(0);
        switch (nom) {
            case "ajoute": LISTE.addAll(mots); return null;
            case "taille": return String.valueOf(LISTE.size());
            case "montre": return LISTE.toString();
            case "retire": return LISTE.remove(mots.get(0)) ? "retiré" : "absent";
            case "inverse": Collections.reverse(LISTE); return null;
            default: return "Commande inconnue : " + nom;
        }
    }

    public static void main(String[] args) {
        String ligne;
        while ((ligne = IO.readln()) != null && !ligne.equals("fin")) {
            String r = traiter(ligne);
            if (r != null) IO.println(r);
        }
    }
}
