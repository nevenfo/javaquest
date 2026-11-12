import java.util.*;

void main() {
    Map<String, Integer> stock = new TreeMap<>();
    String ligne = IO.readln("");
    while (!ligne.equals("fin")) {
        String[] p = ligne.split(";");
        stock.put(p[0].strip(), Integer.parseInt(p[1].strip()));
        ligne = IO.readln("");
    }
    if (stock.isEmpty()) {
        IO.println("Stock vide");
        return;
    }
    List<Map.Entry<String, Integer>> rang = new ArrayList<>(stock.entrySet());
    rang.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
    for (Map.Entry<String, Integer> e : rang) {
        IO.println(e.getKey() + " : " + e.getValue());
    }
}
