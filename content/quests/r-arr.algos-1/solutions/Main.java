void main() {
    int n = Integer.parseInt(IO.readln("Combien de valeurs ? "));
    int[] t = new int[n];
    for (int i = 0; i < n; i++) {
        t[i] = Integer.parseInt(IO.readln("Valeur ? "));
    }
    int min = t[0];
    int max = t[0];
    for (int v : t) {
        if (v < min) min = v;
        if (v > max) max = v;
    }
    IO.println("Écart : " + (max - min));
}
