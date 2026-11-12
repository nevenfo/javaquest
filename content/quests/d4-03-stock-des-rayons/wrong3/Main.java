import java.util.*;

void main() {
    Map<String, Integer> stock = new HashMap<>();
    String ligne = IO.readln("");
    while (!ligne.equals("fin")) {
        String[] p = ligne.split(";");
        stock.merge(p[0].strip(), Integer.parseInt(p[1].strip()), Integer::sum);
        ligne = IO.readln("");
    }
    List<Map.Entry<String, Integer>> rang = new ArrayList<>(stock.entrySet());
    rang.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
    for (Map.Entry<String, Integer> e : rang) {
        IO.println(e.getKey() + " : " + e.getValue());
    }
}
