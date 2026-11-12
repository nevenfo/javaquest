import fr.javaquest.runner.Trace;

/** Le pont du gobelin : appelle ces méthodes, le gobelin réagit dans le monde. */
public class Pont {
    private static int passages;
    private static int refus;

    /** Le gobelin s'écarte : le voyageur peut traverser. */
    public static void laisserPasser() {
        passages++;
        Trace.event("acteur.dire", "gobelin", "Mot juste ! Passe.");
        Trace.event("acteur.deplacer", "gobelin", 0, -1);
        Trace.event("effet", "magie", 2, 0);
    }

    /** Le gobelin bloque le pont : mauvais mot. */
    public static void refuser() {
        refus++;
        Trace.event("acteur.dire", "gobelin", "Faux ! Dehors !");
        Trace.event("effet", "fumee", 2, 0);
    }

    public static int passages() { return passages; }

    public static int refus() { return refus; }

    /** Utilisée par la vérification pour repartir de zéro. */
    public static void reinitialiser() { passages = 0; refus = 0; }
}
