void main() {
    int total = 0;
    int max = 0;
    String mot = IO.readln("Mot ? ");
    while (true) {
        int v = 0;
        for (int i = 0; i < mot.length(); i++) {
            if ("aeiou".indexOf(mot.charAt(i)) >= 0) v++;
        }
        IO.println(mot + " : " + v);
        if (mot.equals("FIN")) break;
        total += v;
        if (v > max) max = v;
        mot = IO.readln("Mot ? ");
    }
    IO.println("Total : " + total);
    IO.println("Maximum : " + max);
}
