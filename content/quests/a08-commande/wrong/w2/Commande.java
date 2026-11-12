public record Commande(String article, String client, int quantite) {

    boolean estGrosse() {
        return quantite >= 10;
    }

    static Commande unique(String client, String article) {
        return new Commande(article, client, 1);
    }
}
