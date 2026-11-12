public class Amphibie implements Roulant, Flottant {
    public int vitesseRoute() { return 30; }
    public int vitesseEau() { return 8; }
    public String deplacement() { return Roulant.super.deplacement(); }
}
