public interface Combattant {
    String nom();
    int force();

    static String presentation(Combattant c) {
        return c.nom();
    }

    static Combattant creer(String nom, int force) {
        return new Combattant() {
            public String nom() { return nom; }
            public int force() { return force; }
        };
    }
}
