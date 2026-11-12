import java.util.*;

public class Main {
    static List<Integer> fusion(List<Integer> a, List<Integer> b) {
        a.addAll(b);
        Collections.sort(a);
        return a;
    }
}
