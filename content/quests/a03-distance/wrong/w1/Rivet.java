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
        return Math.abs(x - autre.x) + Math.abs(y - autre.y);
    }

    public Rivet deplacer(int dx, int dy) {
        return new Rivet(x + dx, y + dy);
    }
}
