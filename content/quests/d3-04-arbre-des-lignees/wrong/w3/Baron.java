public class Baron extends Noble {
    public Baron(String nom, int fortune) {
        super(nom, fortune);
    }

    @Override
    public String titre() {
        return "Baron";
    }

    @Override
    public int tribut() {
        return fortune() / 10;
    }
}
