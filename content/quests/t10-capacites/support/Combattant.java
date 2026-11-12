public interface Combattant {
    int attaque();

    default String cri() {
        return "À l'attaque (" + attaque() + ") !";
    }
}
