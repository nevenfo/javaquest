import java.util.*;

public class Main {
    static void retirerPerimees(List<Integer> fiches) {
        fiches.removeIf(f -> f < 0);
    }

    static void retirerCote(List<Integer> fiches, int cote) {
        fiches.remove(cote);
    }
}
