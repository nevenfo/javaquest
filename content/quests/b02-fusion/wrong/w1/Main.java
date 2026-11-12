import java.util.*;

public class Main {
    static List<Integer> fusion(List<Integer> a, List<Integer> b) {
        List<Integer> res = new ArrayList<>(a);
        res.addAll(b);
        return res;
    }
}
