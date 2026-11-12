void main() {
    int n = Integer.parseInt(IO.readln("Combien d'étages ? "));
    int total = 0;
    for (int k = 1; k <= n; k++) {
        total = 0;
        total += k * k;
    }
    IO.println("Pierres : " + total);
}
