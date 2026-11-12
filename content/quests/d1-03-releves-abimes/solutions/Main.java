static boolean estValide(String s) {
    try {
        Integer.parseInt(s);
        return true;
    } catch (NumberFormatException e) {
        return false;
    }
}

void main() {
    int n = Integer.parseInt(IO.readln("Combien de relevés ? "));
    int valides = 0;
    int somme = 0;
    for (int i = 0; i < n; i++) {
        String ligne = IO.readln("Relevé ? ");
        if (estValide(ligne)) {
            valides++;
            somme += Integer.parseInt(ligne);
        }
    }
    IO.println("Valides : " + valides);
    IO.println("Somme : " + somme);
}
