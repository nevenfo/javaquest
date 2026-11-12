static int diviser(int a, int b) {
    if (b == 0) throw new IllegalArgumentException("zéro");
    return a / b;
}

void main() {
    int[] cas = {4, 0, 3};
    for (int d : cas) {
        try {
            IO.println("100/" + d + " = " + diviser(100, d));
        } catch (IllegalArgumentException e) {
            IO.println("Erreur : " + e.getMessage());
        } finally {
            IO.println("fin " + d);
        }
    }
}
