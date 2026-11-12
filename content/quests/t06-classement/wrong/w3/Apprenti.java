public class Apprenti implements Comparable<Apprenti> {
    private final String nom;
    private final int niveau;

    public Apprenti(String nom, int niveau) {
        this.nom = nom;
        this.niveau = niveau;
    }

    public String getNom() { return nom; }
    public int getNiveau() { return niveau; }

    @Override
    public String toString() { return nom + "(" + niveau + ")"; }

    @Override
    public int compareTo(Apprenti autre) {
        return Integer.compare(niveau, autre.niveau);
    }
}
