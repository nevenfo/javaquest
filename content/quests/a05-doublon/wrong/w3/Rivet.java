public class Rivet {
    private final int x;
    private final int y;

    public Rivet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Rivet autre) {
        return autre != null && x == autre.x && y == autre.y;
    }

    public int hashCode() { return 31 * x + y; }

    public int getX() { return x; }
}
