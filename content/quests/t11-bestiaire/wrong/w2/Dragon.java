public class Dragon extends Creature implements Volant {
    private final int ecailles;
    public Dragon(String nom, int ecailles) { super(nom); this.ecailles = ecailles; }
    public int force() { return 10 + 2 * ecailles; }
    public String presenter() { return super.presenter() + " crache du feu"; }
}
