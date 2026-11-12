void main() {
    String mot = IO.readln("Mot ? ");
    int compte = 0;
    for (int i = 1; i < mot.length(); i++) {
        if (mot.charAt(i) == 'e') {
            compte++;
        }
    }
    IO.println("Lettres e : " + compte);
}
