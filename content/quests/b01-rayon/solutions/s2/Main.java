import java.util.*;

public class Main {
    static boolean emprunter(List<String> rayon, String titre) {
        return rayon.remove(titre);
    }

    static void retirerCourts(List<String> rayon, int min) {
        Iterator<String> it = rayon.iterator();
        while (it.hasNext()) {
            if (it.next().length() < min) it.remove();
        }
    }
}
