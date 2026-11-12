public class Tambour implements Signal {
    private final int coups;

    public Tambour(int coups) {
        this.coups = coups;
    }

    public String nom() {
        return "tambour";
    }

    public int portee() {
        return coups * 4;
    }

    public String annonce(String intro) {
        return intro +  Signal.super.annonce();
    }
}
