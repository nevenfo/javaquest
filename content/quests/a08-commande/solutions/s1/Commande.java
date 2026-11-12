public record Commande(String client, String article, int quantite) {

    boolean estGrosse() {
        return quantite >= 10;
    }

    static Commande unique(String client, String article) {
        return new Commande(client, article, 1);
    }
}
