void main() {
    double poids = 0;
    try {
        poids = Double.parseDouble(IO.readln("Poids ? "));
    } catch (NumberFormatException e) {
        IO.println("Poids invalide.");
    }
    IO.println("Poids : " + poids);
}
