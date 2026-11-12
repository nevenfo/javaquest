void main() {
    int n = Integer.parseInt(IO.readln("Combien de mesures ? "));
    int[] t = new int[n];
    for (int i = 0; i < n; i++) {
        t[i] = Integer.parseInt(IO.readln("Mesure ? "));
    }
    int min = t[0];
    int max = t[0];
    int somme = 0;
    for (int v : t) {
        if (v < min) min = v;
        if (v > max) max = v;
        somme += v;
    }
    IO.println("Min : " + min);
    IO.println("Max : " + max);
    IO.println("Moyenne : " + (double) somme / n);
}
