import fr.javaquest.runner.Trace;
import java.util.ArrayList;
import java.util.List;

/**
 * L'automate de l'observatoire (fourni, en lecture seule). Il évolue sur une grille :
 * <pre>
 *   #  mur     .  case libre     C  case de départ     X  cible
 * </pre>
 * Directions : "haut", "bas", "gauche", "droite". Au départ, l'automate regarde vers "droite".
 * Les bords de la grille comptent comme des murs. Quand tu l'exécutes dans le monde, tu le vois bouger.
 */
public class Compagnon {
    /** Levée quand l'automate fonce dans un mur (ou dans le bord de la grille). */
    public static class MurException extends RuntimeException {
        public MurException(String message) { super(message); }
    }

    private static final String[] DIRS = {"haut", "droite", "bas", "gauche"};
    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};
    // Position de la grille par rapport au héros, pour l'affichage dans le monde.
    private static final int DECALAGE_X = -3, DECALAGE_Y = 2;
    private static final int TUILE_MUR = 152;

    /** Grille d'essai utilisée par {@link #demarrer()}. */
    static final String[] GRILLE_ESSAI = {
        "C.......",
        ".....#..",
        "........",
        "...#....",
        "........",
    };

    private final char[][] grille;
    private final boolean visible;
    private final int cibleX, cibleY;
    private int x, y, cap = 1, pas;
    private final List<String> dits = new ArrayList<>();

    /** Automate silencieux (rien n'est affiché dans le monde) : c'est ce que le harnais de vérification utilise. */
    public Compagnon(String... lignes) { this(false, lignes); }

    /** Automate visible dans le monde, sur la grille d'essai (ou sur la grille donnée). */
    public static Compagnon demarrer(String... lignes) { return new Compagnon(true, lignes.length == 0 ? GRILLE_ESSAI : lignes); }

    private Compagnon(boolean visible, String[] lignes) {
        this.visible = visible;
        grille = new char[lignes.length][];
        int cx = -1, cy = -1, dx = -1, dy = -1;
        for (int j = 0; j < lignes.length; j++) {
            grille[j] = lignes[j].toCharArray();
            if (grille[j].length != lignes[0].length()) throw new IllegalArgumentException("Grille irrégulière (ligne " + j + ")");
            for (int i = 0; i < grille[j].length; i++) {
                if (grille[j][i] == 'C') { cx = i; cy = j; grille[j][i] = '.'; }
                if (grille[j][i] == 'X') { dx = i; dy = j; grille[j][i] = '.'; }
            }
        }
        if (cx < 0) throw new IllegalArgumentException("Grille sans départ (C)");
        x = cx; y = cy; cibleX = dx; cibleY = dy;
        if (visible) {
            for (int j = 0; j < grille.length; j++)
                for (int i = 0; i < grille[j].length; i++)
                    if (grille[j][i] == '#') Trace.event("tuile.poser", DECALAGE_X + i, DECALAGE_Y + j, TUILE_MUR);
            if (dx >= 0) Trace.event("effet", "magie", DECALAGE_X + dx, DECALAGE_Y + dy);
            Trace.event("acteur.apparaitre", "automate", 108, DECALAGE_X + x, DECALAGE_Y + y);
        }
    }

    private static int numero(String dir) {
        for (int k = 0; k < DIRS.length; k++) if (DIRS[k].equals(dir)) return k;
        throw new IllegalArgumentException("Direction inconnue : « " + dir + " » (attendu : haut, bas, gauche ou droite)");
    }

    private boolean mur(int i, int j) {
        return j < 0 || j >= grille.length || i < 0 || i >= grille[j].length || grille[j][i] == '#';
    }

    /** Colonne actuelle (0 = à gauche). */
    public int x() { return x; }

    /** Ligne actuelle (0 = en haut). */
    public int y() { return y; }

    /** Colonne de la cible, ou -1 s'il n'y en a pas. */
    public int cibleX() { return cibleX; }

    /** Ligne de la cible, ou -1 s'il n'y en a pas. */
    public int cibleY() { return cibleY; }

    /** Vrai si l'automate se tient sur la cible. */
    public boolean surCible() { return cibleX >= 0 && x == cibleX && y == cibleY; }

    /** Direction vers laquelle il regarde ("droite" au départ) ; change à chaque déplacement ou virage. */
    public String cap() { return DIRS[cap]; }

    /** Nombre de pas faits depuis le départ. */
    public int pas() { return pas; }

    /** Ce que le capteur voit dans cette direction : "mur", "libre" ou "cible". */
    public String capteur(String dir) {
        int k = numero(dir), i = x + DX[k], j = y + DY[k];
        if (mur(i, j)) return "mur";
        return i == cibleX && j == cibleY ? "cible" : "libre";
    }

    /** Vrai si un mur (ou le bord de la grille) bloque la case voisine dans cette direction. */
    public boolean devantMur(String dir) { return capteur(dir).equals("mur"); }

    /** Fait un pas dans cette direction (et regarde de ce côté). Lève {@link MurException} si un mur bloque. */
    public void avancer(String dir) {
        int k = numero(dir), i = x + DX[k], j = y + DY[k];
        cap = k;
        if (mur(i, j))
            throw new MurException("L'automate heurte un mur : de (" + x + ", " + y + ") vers « " + dir + " »");
        x = i; y = j; pas++;
        if (visible) {
            Trace.event("acteur.deplacer", "automate", DX[k], DY[k]);
            if (surCible()) { Trace.event("effet", "etincelle", DECALAGE_X + x, DECALAGE_Y + y); Trace.event("effet", "coeur", DECALAGE_X + x, DECALAGE_Y + y); }
        }
    }

    /** Fait un pas dans la direction où il regarde. */
    public void avancer() { avancer(cap()); }

    /** Tourne d'un quart de tour vers la droite (haut -> droite -> bas -> gauche -> haut). */
    public void tourner() {
        cap = (cap + 1) % 4;
        if (visible) Trace.event("effet", "etincelle", DECALAGE_X + x, DECALAGE_Y + y);
    }

    /** L'automate dit une phrase (bulle dans le monde). */
    public void dire(String texte) {
        dits.add(texte);
        if (visible) Trace.event("acteur.dire", "automate", texte);
    }

    /** Phrases dites jusqu'ici, dans l'ordre. */
    public List<String> dits() { return new ArrayList<>(dits); }
}
