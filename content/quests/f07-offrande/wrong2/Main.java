void main() {
    int glands = 0;
    boolean accepte = false;
    while (!accepte) {
        try {
            glands = Integer.parseInt(IO.readln("Combien de glands ? "));
            accepte = true;
        } catch (NumberFormatException e) {
            IO.println("Saisie invalide.");
        }
    }
    IO.println("Merci : " + glands + " glands");
}
