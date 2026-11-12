import java.util.Random;

public class Tirage {
    private static int lances = 0;

    public static int lancer(Random rng, int faces) {
        lances++;
        if (faces < 1) throw new IllegalArgumentException("faces < 1");
        return new Random().nextInt(faces) + 1;
    }

    public static int getLances() { return lances; }
}
