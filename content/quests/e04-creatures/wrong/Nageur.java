public interface Nageur {
    String nom();
    default void nager(int brasses) { IO.println(nom() + " nage " + brasses + " brasses"); }
}
