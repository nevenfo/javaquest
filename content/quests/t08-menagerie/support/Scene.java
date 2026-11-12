import fr.javaquest.runner.Trace;

/** Anime les capacités d'une créature sur la scène (fournie : lecture seule). */
public final class Scene {
    private Scene() { }

    public static void presenter(String acteur, String nom, Object creature) {
        if (creature instanceof Marcheur m) {
            String t = m.marcher();
            Trace.event("acteur.deplacer", acteur, 2, 0);
            Trace.event("acteur.dire", acteur, t);
            System.out.println(t);
        } else {
            System.out.println(nom + " ne sait pas marcher");
        }
        if (creature instanceof Nageur n) {
            String t = n.nager();
            Trace.event("effet", "eau", 3, 1);
            Trace.event("acteur.dire", acteur, t);
            System.out.println(t);
        } else {
            System.out.println(nom + " ne sait pas nager");
        }
        if (creature instanceof Volant v) {
            String t = v.voler();
            Trace.event("acteur.deplacer", acteur, 0, -2);
            Trace.event("acteur.dire", acteur, t);
            System.out.println(t);
        } else {
            System.out.println(nom + " ne sait pas voler");
        }
    }
}
