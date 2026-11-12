import java.util.*;

void main() {
    List<String> l = new ArrayList<>(List.of("or", "fer", "or", "cuivre"));
    Set<String> s = new TreeSet<>(l);
    Map<String, Integer> m = new TreeMap<>();
    for (String x : l) m.merge(x, 1, Integer::sum);
    List<Integer> n = new ArrayList<>(List.of(10, 20, 30));
    n.remove(1);
    n.remove(Integer.valueOf(10));
    IO.println(l.size() + " " + s.size());
    IO.println(s);
    IO.println(m);
    IO.println(m.get("or") + " " + m.getOrDefault("zinc", 0));
    IO.println(n);
    IO.println(m.get("zinc"));
}
