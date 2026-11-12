public class PompeElixir {
    private static final int[] BILLETS = {50, 20, 10, 5};

    private int stock;
    private final int prixLitre;
    private int recette = 0;

    public PompeElixir(int stock, int prixLitre) {
        if (stock < 0) throw new IllegalArgumentException("stock négatif");
        if (prixLitre <= 0) throw new IllegalArgumentException("prix invalide");
        this.stock = stock;
        this.prixLitre = prixLitre;
    }

    public int getStock() { return stock; }
    public int getRecette() { return recette; }

    public int servir(int litres) {
        if (litres <= 0) throw new IllegalArgumentException("quantité invalide");
        if (litres > stock) throw new IllegalStateException("stock insuffisant");
        int cout = litres * prixLitre;
        stock -= litres;
        recette += cout;
        return cout;
    }

    public static int compterBillets(int somme) {
        if (somme < 0 || somme % 5 != 0) throw new IllegalArgumentException("somme impossible à payer en billets");
        int n = 0;
        for (int b : BILLETS) {
            n += somme / b;
            somme %= b;
        }
        return n;
    }
}
