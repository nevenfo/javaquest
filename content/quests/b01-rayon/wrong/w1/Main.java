import java.util.*;

public class Main {
    static boolean emprunter(List<String> rayon, String titre) {
        return rayon.remove(titre);
    }

    static void retirerCourts(List<String> rayon, int min) {
        for (String t : rayon) {
            if (t.length() < min) rayon.remove(t);
        }
    }
}
