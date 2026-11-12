void main() {
    double poids = 0;
    boolean ok = false;
    while (!ok) {
        try {
            poids = Double.parseDouble(IO.readln("Poids ? "));
            ok = true;
        } catch (NumberFormatException e) {
            IO.println("Poids invalide.");
        }
    }
    IO.println("Poids : " + poids);
}
