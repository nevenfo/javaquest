void main() {
    int[] comptes = new int[10];
    int n = 0;
    String mot = IO.readln("Mot ? ");
    while (!mot.equals("FIN")) {
        int v = 0;
        for (int i = 0; i < mot.length(); i++) {
            if ("aeiou".indexOf(mot.charAt(i)) >= 0) v++;
        }
        comptes[n] = v;
        n++;
        IO.println(mot + " : " + v);
        mot = IO.readln("Mot ? ");
    }
    int total = 0;
    int max = 0;
    for (int i = 0; i < n; i++) {
        total += comptes[i];
        if (comptes[i] > max) max = comptes[i];
    }
    IO.println("Total : " + total);
    IO.println("Maximum : " + max);
}
