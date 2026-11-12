public class Main {
    public static void main(String[] args) {
        int total = 0;
        while (true) {
            String ligne = IO.readln();
            if (ligne == null || ligne.equals("fin")) break;
            String[] mots = ligne.trim().split("\\s+");
            switch (mots[0]) {
                case "ajoute" -> total = Integer.parseInt(mots[1]);
                case "montre" -> IO.println("Total : " + total);
                default -> IO.println("Commande inconnue : " + mots[0]);
            }
        }
    }
}
