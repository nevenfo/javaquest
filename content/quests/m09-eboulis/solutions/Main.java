void main() {
    int n = Integer.parseInt(IO.readln("Nombre de filons ? "));
    int[] valeurs = new int[n];
    for (int i = 0; i < n; i++) {
        valeurs[i] = Integer.parseInt(IO.readln("Valeur ? "));
    }
    for (int i = n - 1; i >= 0; i--) {
        IO.println(valeurs[i]);
    }
}
