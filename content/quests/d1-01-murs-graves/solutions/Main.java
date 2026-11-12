void main() {
    int ombres = 0;
    int lettresE = 0;
    String mot = IO.readln("Inscription ? ");
    while (!mot.equals("fin")) {
        if (mot.equals("ombre")) {
            ombres++;
        }
        for (int i = 0; i < mot.length(); i++) {
            if (mot.charAt(i) == 'e') {
                lettresE++;
            }
        }
        mot = IO.readln("Inscription ? ");
    }
    IO.println("Ombres : " + ombres);
    IO.println("Lettres e : " + lettresE);
}
