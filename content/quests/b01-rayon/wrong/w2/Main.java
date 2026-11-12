import java.util.*;

public class Main {
    static boolean emprunter(List<String> rayon, String titre) {
        return rayon.remove(titre);
    }

    static void retirerCourts(List<String> rayon, int min) {
        for (int i = 0; i < rayon.size(); i++) {
            if (rayon.get(i).length() < min) rayon.remove(i);
        }
    }
}
