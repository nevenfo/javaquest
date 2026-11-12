/**
 * Sonde de débogage : affiche une valeur dans le panneau « Observations » du jeu,
 * avec la ligne de ton code où elle a été relevée. N'influence pas l'exécution.
 * Exemple : Sonde.voir("mana", mana);
 */
public final class Sonde {
    private Sonde() { }

    public static void voir(String nom, Object valeur) {
        fr.javaquest.runner.Trace.watch(nom, valeur);
    }
}
