public record Potion(String nom, int dose) {
    public Potion {
        if (dose < 0) throw new IllegalArgumentException("dose négative");
    }

    Potion diluer() {
        return new Potion(nom, dose / 2);
    }
}
