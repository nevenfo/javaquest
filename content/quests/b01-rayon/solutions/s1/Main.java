import java.util.*;

public class Main {
    static boolean emprunter(List<String> rayon, String titre) {
        return rayon.remove(titre);
    }

    static void retirerCourts(List<String> rayon, int min) {
        rayon.removeIf(t -> t.length() < min);
    }
}
