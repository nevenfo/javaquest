import java.util.*;

void main() {
    Map<String, Integer> stock = new TreeMap<>();
    String ligne = IO.readln("");
    while (!ligne.equals("fin")) {
        String[] p = ligne.split(";");
        stock.merge(p[0].strip(), Integer.parseInt(p[1].strip()), Integer::sum);
        ligne = IO.readln("");
    }
    if (stock.isEmpty()) {
        IO.println("Stock vide");
        return;
    }
    for (Map.Entry<String, Integer> e : stock.entrySet()) {
        IO.println(e.getKey() + " : " + e.getValue());
    }
}
