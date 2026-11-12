public class Fiole {
    private final String liquide;
    private final int contenance;

    public Fiole(String liquide, int contenance) {
        this.liquide = liquide;
        this.contenance = contenance;
    }

    public String getLiquide() { return liquide; }
    public int getContenance() { return contenance; }
    public boolean estVide() { return contenance < 0; }
}
