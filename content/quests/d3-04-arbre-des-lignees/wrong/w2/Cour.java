import java.util.Arrays;

public class Cour {
    public static Noble[] classer(Noble[] nobles) {
        Noble[] copie = Arrays.copyOf(nobles, nobles.length);
        Arrays.sort(copie);
        return copie;
    }
}
