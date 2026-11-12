public class Comte extends Noble {
    public Comte(String nom, int fortune) {
        super(nom, fortune);
    }

    @Override
    public String titre() {
        return "Comte";
    }

    @Override
    public int tribut() {
        return fortune() / 5 + 50;
    }
}
