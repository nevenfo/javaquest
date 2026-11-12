import java.util.*;

public class Main {
    static int forceTotale(List<Creature> l) {
        int t = 0;
        for (Creature c : l) t += c.force();
        return t;
    }

    static List<String> rapport(List<Creature> l) {
        List<String> r = new ArrayList<>();
        for (Creature c : l) r.add(c.presenter());
        return r;
    }
}
