import java.util.*;

public class Pioche {
    public static List<String> tirer(Random rng, List<String> cartes, int n) {
        if (n < 0 || n > cartes.size()) throw new IllegalArgumentException("n invalide");
        List<String> reste = new ArrayList<>(cartes);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            res.add(reste.remove(rng.nextInt(reste.size())));
        }
        return res;
    }
}
