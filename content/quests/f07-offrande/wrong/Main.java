void main() {
    int glands = 0;
    boolean accepte = false;
    while (!accepte) {
        try {
            glands = Integer.parseInt(IO.readln("Combien de glands ? "));
            if (glands >= 1 && glands < 10) {
                accepte = true;
            } else {
                IO.println("Entre 1 et 10 !");
            }
        } catch (NumberFormatException e) {
            IO.println("Saisie invalide.");
        }
    }
    IO.println("Merci : " + glands + " glands");
}
