public class Bourse {
    private int solde;

    public Bourse(int solde) {
        this.solde = solde;
    }

    public int getSolde() { return solde; }

    public void deposer(int montant) {
        if (montant <= 0) throw new IllegalArgumentException("montant invalide");
        solde += montant;
    }

    public void retirer(int montant) {
        if (montant <= 0) throw new IllegalArgumentException("montant invalide");
        solde -= montant;
        if (solde < 0) throw new IllegalStateException("solde insuffisant");
    }
}
