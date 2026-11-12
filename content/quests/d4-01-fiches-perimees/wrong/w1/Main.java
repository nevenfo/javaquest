import java.util.*;

public class Main {
    static void retirerPerimees(List<Integer> fiches) {
        for (int i = 0; i < fiches.size(); i++) {
            if (fiches.get(i) < 0) {
                fiches.remove(i);
            }
        }
    }

    static void retirerCote(List<Integer> fiches, int cote) {
        fiches.remove(Integer.valueOf(cote));
    }
}
