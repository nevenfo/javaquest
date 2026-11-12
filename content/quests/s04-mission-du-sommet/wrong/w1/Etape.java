public record Etape(String nom, int x, int y) {
    public Etape {
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Le nom d'une étape ne peut pas être vide.");
        if (x < 0 || y < 0) throw new IllegalArgumentException("Coordonnées négatives : (" + x + ", " + y + ")");
    }
}
