import fr.javaquest.runner.Trace;
import java.util.ArrayList;
import java.util.List;

/** Le golem des mines : appelle ces méthodes, il agit dans le monde. */
public class Golem {
    private static int x;
    private static int creuses;
    private static final List<Integer> pierres = new ArrayList<>();

    /** Le golem avance d'une case vers la droite. */
    public static void avancer() {
        x++;
        Trace.event("acteur.deplacer", "golem", 1, 0);
    }

    /** Le golem creuse la case où il se trouve. */
    public static void creuser() {
        creuses++;
        Trace.event("effet", "etincelle", 2 + x, 0);
    }

    /** Le golem pose une pierre d'étai sur la case où il se trouve. */
    public static void poserPierre() {
        pierres.add(x);
        Trace.event("effet", "fumee", 2 + x, 0);
        Trace.event("compteur", "pierres", pierres.size());
    }

    /** Numéro de la case actuelle (0 au départ). */
    public static int position() { return x; }

    public static int creuses() { return creuses; }

    public static List<Integer> positionsPierres() { return new ArrayList<>(pierres); }

    /** Utilisée par la vérification pour repartir de zéro. */
    public static void reinitialiser() { x = 0; creuses = 0; pierres.clear(); }
}
