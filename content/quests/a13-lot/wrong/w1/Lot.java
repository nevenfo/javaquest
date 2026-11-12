public record Lot(String metal, int quantite) {
    public Lot {
        if (metal == null || metal.isBlank()) throw new IllegalArgumentException("métal manquant");
    }

    Lot ajouter(int n) {
        return new Lot(metal, quantite + n);
    }

    boolean estVide() {
        return quantite == 0;
    }

    static Lot vide(String metal) {
        return new Lot(metal, 0);
    }
}
