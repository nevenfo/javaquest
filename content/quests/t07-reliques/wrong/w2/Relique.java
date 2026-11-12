public class Relique implements Comparable<Relique> {
    private final String nom;
    private final int poids;

    public Relique(String nom, int poids) {
        this.nom = nom;
        this.poids = poids;
    }

    public String getNom() { return nom; }
    public int getPoids() { return poids; }

    @Override
    public String toString() { return nom + " (" + poids + " kg)"; }

    @Override
    public int compareTo(Relique autre) {
        return Integer.compare(autre.poids, poids);
    }
}
