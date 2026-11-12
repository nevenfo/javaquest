public class Rivet {
    private final int x;
    private final int y;

    public Rivet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public double distanceTo(Rivet autre) {
        int dx = x - autre.x;
        int dy = y - autre.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Rivet deplacer(int dx, int dy) {
        return new Rivet(x + dx, y + dy);
    }
}
