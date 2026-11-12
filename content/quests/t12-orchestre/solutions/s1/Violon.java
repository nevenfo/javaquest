public class Violon extends Instrument {
    private final int cordes;
    public Violon(int cordes) { super("Violon"); this.cordes = cordes; }
    public String son() { return "vi".repeat(cordes); }
    public String jouer() { return super.jouer() + " (archet)"; }
}
