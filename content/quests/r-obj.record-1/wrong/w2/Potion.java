public record Potion(String nom, int dose) {
    Potion diluer() {
        return new Potion(nom, dose / 2);
    }
}
