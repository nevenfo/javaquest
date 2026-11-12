void main() {
    String mot = IO.readln("Mot ? ");
    boolean miroir = true;
    for (int i = 0; i < mot.length() / 2; i++) {
        if (mot.charAt(i) != mot.charAt(mot.length() - 1 - i)) {
            miroir = false;
        }
    }
    if (miroir) {
        IO.println("oui");
    } else {
        IO.println("non");
    }
}
