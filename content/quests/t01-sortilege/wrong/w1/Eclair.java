public class Eclair implements Sortilege {
    private final int niveau;

    public Eclair(int niveau) {
        this.niveau = niveau;
    }

    public String nom() { return "Eclair"; }

    public int degats() { return 4 * niveau; }
}
