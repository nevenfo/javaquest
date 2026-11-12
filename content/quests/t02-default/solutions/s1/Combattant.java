public interface Combattant {
    String nom();
    int force();

    default String presentation() {
        return nom() + " (force " + force() + ")";
    }

    static Combattant creer(String nom, int force) {
        return new Combattant() {
            public String nom() { return nom; }
            public int force() { return force; }
        };
    }
}
