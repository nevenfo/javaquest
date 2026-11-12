void main() {
    int total = 0;
    int max = 0;
    String mot = IO.readln("Mot ? ");
    while (!mot.equals("FIN")) {
        int v = 0;
        for (int i = 0; i < mot.length(); i++) {
            char c = mot.charAt(i);
            if (c == 'a' || c == 'e') v++;
        }
        IO.println(mot + " : " + v);
        total += v;
        if (v > max) max = v;
        mot = IO.readln("Mot ? ");
    }
    IO.println("Total : " + total);
    IO.println("Maximum : " + max);
}
