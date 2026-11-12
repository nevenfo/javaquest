void main() {
    int fagots = 0;
    boolean valide = false;
    while (!valide) {
        try {
            fagots = Integer.parseInt(IO.readln("Combien de fagots ? "));
            valide = true;
        } catch (NumberFormatException e) {
            IO.println("Ce n'est pas un nombre !");
        }
    }
    IO.println("Fagots : " + fagots);
}
