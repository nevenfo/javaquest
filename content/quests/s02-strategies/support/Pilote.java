/** Fait avancer l'automate en suivant une stratégie. */
public class Pilote {
    /**
     * Répète « avancer dans la direction choisie » jusqu'à la cible, jusqu'à ce que la stratégie renvoie null,
     * ou après {@code maxPas} pas. Renvoie vrai si l'automate est sur la cible.
     */
    public static boolean conduire(Compagnon c, Strategie s, int maxPas) {
        for (int i = 0; i < maxPas && !c.surCible(); i++) {
            String dir = s.choisir(c);
            if (dir == null) break;
            c.avancer(dir);
        }
        return c.surCible();
    }
}
