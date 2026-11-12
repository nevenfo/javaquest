public record Ordonnance(String potion, int doses) {
    public Ordonnance {
        if (doses <= 0) throw new IllegalArgumentException("doses <= 0");
    }

    public Ordonnance doubler() {
        return new Ordonnance(potion, doses * 2);
    }
}
