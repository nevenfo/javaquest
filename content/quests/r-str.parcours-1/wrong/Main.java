void main() {
    String mot = IO.readln("Mot ? ");
    char lettre = IO.readln("Lettre ? ").charAt(0);
    int indice = -1;
    for (int i = 0; i < mot.length(); i++) {
        if (mot.charAt(i) == lettre) {
            indice = i;
        }
    }
    IO.println("Indice : " + indice);
}
