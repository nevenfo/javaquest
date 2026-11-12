import java.util.*;

public class Main {
    private static final Map<String, Commande> COMMANDES = new HashMap<>();

    static void enregistrer(String nom, Commande cmd) { COMMANDES.put(nom, cmd); }

    static String traiter(Compagnon c, String ligne) {
        if (ligne.isBlank()) return null;
        List<String> mots = new ArrayList<>(Arrays.asList(ligne.trim().split("\s+")));
        String nom = mots.remove(0);
        try {
            switch (nom) {
                case "avance" -> {
                    if (mots.size() != 1) throw new CommandeException("nombre attendu");
                    int n = Integer.parseInt(mots.get(0));
                    if (n < 0) throw new CommandeException("nombre attendu");
                    for (int i = 0; i < n; i++) c.avancer();
                }
                case "tourne" -> c.tourner();
                case "dis" -> {
                    if (mots.isEmpty()) throw new CommandeException("rien à dire");
                    c.dire(String.join(" ", mots));
                }
                default -> { return "Commande inconnue : " + nom; }
            }
            return "ok";
        } catch (CommandeException | Compagnon.MurException | NumberFormatException e) {
            return "Erreur : " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        Compagnon c = Compagnon.demarrer();
        String ligne;
        while ((ligne = IO.readln()) != null && !ligne.equals("fin")) {
            String r = traiter(c, ligne);
            if (r != null) IO.println(r);
        }
    }
}
