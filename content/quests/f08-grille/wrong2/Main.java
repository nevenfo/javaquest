void main() {
    String mot = IO.readln("Mot de passe ? ");
    if (!mot.equals("sapin-d'or")) {
        IO.println("Grille fermée.");
        return;
    }
    int torches = Integer.parseInt(IO.readln("Torches ? "));
    IO.println("Grille ouverte.");
    if (torches < 3) {
        IO.println("Trop sombre");
    } else if (torches < 7) {
        IO.println("Bien éclairé");
    } else {
        IO.println("Aveuglant");
    }
}
