public interface Combattant {
    String nom();
    int force();

    String presentation();

    static Combattant creer(String nom, int force) {
        return null;
    }
}
