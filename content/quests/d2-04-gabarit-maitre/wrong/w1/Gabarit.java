public record Gabarit(String modele, int taille) {
    public Gabarit {
        if (modele == null || modele.isBlank()) {
            throw new IllegalArgumentException("modèle invalide");
        }
        if (taille < 1 || taille > 20) {
            throw new IllegalArgumentException("taille invalide");
        }
    }

    public static Gabarit unitaire(String modele) {
        return new Gabarit(modele, 1);
    }

    public int volume() {
        return taille * taille * taille;
    }

    public Gabarit agrandir(int delta) {
        return new Gabarit(modele, taille + delta);
    }
}
