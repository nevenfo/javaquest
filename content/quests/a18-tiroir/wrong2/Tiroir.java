public class Tiroir {
    public final int capacite;
    public int contenu;

    public Tiroir(int capacite) {
        this.capacite = capacite;
    }

    public boolean ranger(int n) {
        if (n <= 0 || contenu + n > capacite) return false;
        contenu += n;
        return true;
    }

    public boolean sortir(int n) {
        if (n <= 0 || n > contenu) return false;
        contenu -= n;
        return true;
    }

    public int getContenu() { return contenu; }

    public int getCapacite() { return capacite; }

    public boolean estVide() { return contenu == 0; }

    public boolean estPlein() { return contenu == capacite; }
}
