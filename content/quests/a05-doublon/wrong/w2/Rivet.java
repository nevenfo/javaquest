public class Rivet {
    private final int x;
    private final int y;

    public Rivet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        Rivet r = (Rivet) o;
        return x == r.x && y == r.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

    public int getX() { return x; }
}
