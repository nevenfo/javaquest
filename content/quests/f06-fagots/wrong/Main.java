void main() {
    int fagots = 0;
    try {
        fagots = Integer.parseInt(IO.readln("Combien de fagots ? "));
    } catch (NumberFormatException e) {
        IO.println("Ce n'est pas un nombre !");
    }
    IO.println("Fagots : " + fagots);
}
