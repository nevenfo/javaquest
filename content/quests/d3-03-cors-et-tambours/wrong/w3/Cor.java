public class Cor implements Signal {
    private final int souffle;

    public Cor(int souffle) {
        this.souffle = souffle;
    }

    public String nom() {
        return "cor";
    }

    public String annonce() {
        return "cor : " + portee() + " pas";
    }

    public int portee() {
        return souffle * 10;
    }
}
