public class Flute extends Instrument {
    private final boolean aigue;
    public Flute(boolean aigue) { super("Flûte"); this.aigue = aigue; }
    public String son() { return aigue ? "tiii" : "fouuu"; }
}
