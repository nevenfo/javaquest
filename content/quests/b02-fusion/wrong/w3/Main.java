import java.util.*;

public class Main {
    static List<Integer> fusion(List<Integer> a, List<Integer> b) {
        List<Integer> res = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i) <= b.get(j)) res.add(a.get(i++));
            else res.add(b.get(j++));
        }
        return res;
    }
}
