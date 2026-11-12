void main() {
    int[] t = new int[8];
    int i = 0;
    int v = 1;
    while (v < 40) {
        t[i] = v;
        v = v * 2 + 1;
        i++;
    }
    IO.println(i);
    IO.println(java.util.Arrays.toString(t));
    int k = t.length - 1;
    while (k >= 0 && t[k] == 0) {
        k--;
    }
    IO.println(k + " " + t[k]);
}
