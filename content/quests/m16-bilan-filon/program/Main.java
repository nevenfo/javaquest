void main() {
    int[] t = {4, 9, 2, 9, 5};
    int max = t[0];
    int pos = 0;
    int somme = 0;
    for (int i = 0; i < t.length; i++) {
        somme += t[i];
        if (t[i] > max) {
            max = t[i];
            pos = i;
        }
    }
    IO.println(max + " en " + pos);
    IO.println(somme);
    int petit = 0;
    for (int i = 1; i < t.length; i++) {
        if (t[i] < t[petit]) petit = i;
    }
    int tmp = t[0];
    t[0] = t[petit];
    t[petit] = tmp;
    IO.println(java.util.Arrays.toString(t));
}
