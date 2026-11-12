import java.util.*;

public class Main {
    static final Map<String, Commande> COMMANDES = new HashMap<>();

    static {
        COMMANDES.put("somme", args -> String.valueOf(args.stream().mapToInt(Integer::parseInt).sum()));
        COMMANDES.put("compte", args -> String.valueOf(args.size()));
        COMMANDES.put("echo", args -> String.join(" ", args));
    }

    static String traiter(String ligne) {
        List<String> mots = new ArrayList<>(Arrays.asList(ligne.trim().split("\\s+")));
        String nom = mots.remove(0);
        Commande c = COMMANDES.get(nom);
        return c == null ? "Commande inconnue : " + nom : c.executer(mots);
    }

    public static void main(String[] args) {
        String ligne;
        while ((ligne = IO.readln()) != null && !ligne.equals("fin")) IO.println(traiter(ligne));
    }
}
