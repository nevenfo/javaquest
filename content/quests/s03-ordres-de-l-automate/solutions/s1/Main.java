import java.util.*;

public class Main {
    private static final Map<String, Commande> COMMANDES = new HashMap<>();

    static {
        enregistrer("avance", (c, args) -> {
            if (args.size() != 1) throw new CommandeException("nombre attendu");
            int n;
            try {
                n = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                throw new CommandeException("nombre attendu");
            }
            if (n < 0) throw new CommandeException("nombre attendu");
            for (int i = 0; i < n; i++) c.avancer();
        });
        enregistrer("tourne", (c, args) -> c.tourner());
        enregistrer("dis", (c, args) -> {
            if (args.isEmpty()) throw new CommandeException("rien à dire");
            c.dire(String.join(" ", args));
        });
    }

    static void enregistrer(String nom, Commande cmd) { COMMANDES.put(nom, cmd); }

    static String traiter(Compagnon c, String ligne) {
        if (ligne.isBlank()) return null;
        List<String> mots = new ArrayList<>(Arrays.asList(ligne.trim().split("\s+")));
        String nom = mots.remove(0);
        Commande cmd = COMMANDES.get(nom);
        if (cmd == null) return "Commande inconnue : " + nom;
        try {
            cmd.executer(c, mots);
            return "ok";
        } catch (CommandeException | Compagnon.MurException e) {
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
