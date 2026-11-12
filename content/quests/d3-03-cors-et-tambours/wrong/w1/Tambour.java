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

    @Override
    public String annonce() {
        return "BOUM ! " + Signal.super.annonce();
    }
}
