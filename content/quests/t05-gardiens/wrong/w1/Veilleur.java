public class Veilleur extends Gardien {
    private final int vigilance;

    public Veilleur(String nom, int vigilance) {
        super(nom);
        this.vigilance = vigilance;
    }

    @Override
    public int force() { return vigilance * 2; }

    @Override
    public String presentation() {
        return nom + " veille";
    }
}
