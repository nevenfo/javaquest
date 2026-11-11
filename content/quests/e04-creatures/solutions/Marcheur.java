public interface Marcheur {
    String nom();
    default void marcher(int pas) { IO.println(nom() + " marche " + pas + " pas"); }
}
