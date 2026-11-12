public class Golem extends Gardien {
    public Golem(String nom) {
        super(nom);
    }

    @Override
    public int force() { return 20; }

    @Override
    public String presentation() { return "[" + nom + " | force 20]"; }
}
