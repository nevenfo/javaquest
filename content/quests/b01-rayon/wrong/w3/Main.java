import java.util.*;

public class Main {
    static boolean emprunter(List<String> rayon, String titre) {
        for (int i = 0; i < rayon.size(); i++) {
            if (rayon.get(i) == titre) { rayon.remove(i); return true; }
        }
        return false;
    }

    static void retirerCourts(List<String> rayon, int min) {
        rayon.removeIf(t -> t.length() < min);
    }
}
