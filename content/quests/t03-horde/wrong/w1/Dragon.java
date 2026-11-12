public class Dragon implements Monstre {
    private final int envergure;

    public Dragon(int envergure) { this.envergure = envergure; }

    public String cri() { return "ROAAR !"; }
    public int attaque() { return 10 + envergure; }
}
