public interface Volant {
    String nom();
    default void voler(int battements) { IO.println(nom() + " vole " + battements + " battements"); }
}
