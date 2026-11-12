public abstract class Noble implements Comparable<Noble> {
    private final String nom;
    private final int fortune;

    public Noble(String nom, int fortune) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("nom invalide");
        }
        if (fortune < 0) {
            throw new IllegalArgumentException("fortune négative");
        }
        this.nom = nom.strip();
        this.fortune = fortune;
    }

    public String nom() {
        return nom;
    }

    public int fortune() {
        return fortune;
    }

    public abstract String titre();

    public abstract int tribut();

    @Override
    public int compareTo(Noble autre) {
        int ordre = Integer.compare(tribut(), autre.tribut());
        if (ordre != 0) {
            return ordre;
        }
        return nom.compareTo(autre.nom);
    }

    @Override
    public String toString() {
        return titre() + " " + nom + " (" + tribut() + ")";
    }
}
