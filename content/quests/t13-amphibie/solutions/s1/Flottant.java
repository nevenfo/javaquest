public interface Flottant {
    int vitesseEau();

    default String deplacement() {
        return "flotte à " + vitesseEau() + " km/h";
    }
}
