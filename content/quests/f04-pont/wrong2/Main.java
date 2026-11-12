void main() {
    String mot = IO.readln("Mot de passe ? ");
    if (mot.equalsIgnoreCase("pomme-de-pin")) {
        Pont.laisserPasser();
    } else {
        Pont.refuser();
    }
}
