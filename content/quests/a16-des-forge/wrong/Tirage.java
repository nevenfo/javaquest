import java.util.Random;

public class Tirage {
    private static int lances = 0;

    public static int lancer(Random rng, int faces) {
        if (faces < 1) throw new IllegalArgumentException("faces < 1");
        lances++;
        return rng.nextInt(faces);
    }

    public static int getLances() { return lances; }
}
