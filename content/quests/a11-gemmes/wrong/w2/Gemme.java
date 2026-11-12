import java.util.Random;

public record Gemme(int valeur) {
    static Gemme aleatoire(Random rng, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return new Gemme(min + (int) (Math.random() * (max - min + 1)));
    }
}
