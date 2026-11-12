public class Bourse {
    private int solde;

    public Bourse(int solde) {
        if (solde < 0) throw new IllegalArgumentException("solde négatif");
        this.solde = solde;
    }

    public int getSolde() { return solde; }

    public void deposer(int montant) {
        if (montant <= 0) throw new IllegalArgumentException("montant invalide");
        solde += montant;
    }

    public void retirer(int montant) {
        if (montant <= 0) throw new IllegalArgumentException("montant invalide");
        if (montant > solde) throw new IllegalStateException("solde insuffisant");
        solde -= montant;
    }
}
