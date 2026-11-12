public interface Roulant {
    int vitesseRoute();

    default String deplacement() {
        return "roule à " + vitesseRoute() + " km/h";
    }
}
