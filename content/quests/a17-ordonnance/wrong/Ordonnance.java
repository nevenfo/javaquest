public record Ordonnance(String potion, int doses) {
    public Ordonnance doubler() {
        return new Ordonnance(potion, doses * 2);
    }
}
