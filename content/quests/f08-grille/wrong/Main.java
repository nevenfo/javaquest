void main() {
    String mot = IO.readln("Mot de passe ? ");
    if (mot != "sapin-d'or") {
        IO.println("Grille fermée.");
        return;
    }
    int torches = 0;
    boolean valide = false;
    while (!valide) {
        try {
            torches = Integer.parseInt(IO.readln("Torches ? "));
            valide = true;
        } catch (NumberFormatException e) {
            IO.println("Saisie invalide.");
        }
    }
    IO.println("Grille ouverte.");
    if (torches < 3) {
        IO.println("Trop sombre");
    } else if (torches < 7) {
        IO.println("Bien éclairé");
    } else {
        IO.println("Aveuglant");
    }
}
