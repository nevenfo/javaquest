public class Alliage {
    private final String nom;
    private final int cuivre;
    private final int etain;

    public Alliage(String nom, int cuivre, int etain) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("nom invalide");
        }
        if (cuivre < 0 || etain < 0 || cuivre + etain != 100) {
            throw new IllegalArgumentException("proportions invalides");
        }
        this.nom = nom;
        this.cuivre = cuivre;
        this.etain = etain;
    }

    public String getNom() {
        return nom;
    }

    public int getCuivre() {
        return cuivre;
    }

    public int getEtain() {
        return etain;
    }

    public boolean estBronze() {
        return cuivre >= 80;
    }

    @Override
    public String toString() {
        return "Alliage " + nom + " (" + cuivre + "% cuivre, " + etain + "% etain)";
    }
}
