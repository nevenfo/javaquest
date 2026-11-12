public class Cor implements Signal {
    private final int souffle;

    public Cor(int souffle) {
        this.souffle = souffle;
    }

    public String nom() {
        return "cor";
    }

    public int portee() {
        return souffle * 10;
    }
}
