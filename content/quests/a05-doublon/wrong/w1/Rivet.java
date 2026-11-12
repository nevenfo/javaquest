public class Rivet {
    private final int x;
    private final int y;

    public Rivet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Rivet r && x == r.x && y == r.y;
    }

    public int getX() { return x; }
}
