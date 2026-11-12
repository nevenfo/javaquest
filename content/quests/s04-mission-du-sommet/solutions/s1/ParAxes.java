public class ParAxes implements Trajet {
    @Override
    public void rejoindre(Compagnon c, Etape e) {
        while (c.x() < e.x()) c.avancer("droite");
        while (c.x() > e.x()) c.avancer("gauche");
        while (c.y() < e.y()) c.avancer("bas");
        while (c.y() > e.y()) c.avancer("haut");
    }
}
