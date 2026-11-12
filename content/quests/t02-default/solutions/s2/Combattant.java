public interface Combattant {
    String nom();
    int force();

    default String presentation() {
        return nom() + " (force " + force() + ")";
    }

    record Simple(String nom, int force) implements Combattant { }

    static Combattant creer(String nom, int force) {
        return new Simple(nom, force);
    }
}
