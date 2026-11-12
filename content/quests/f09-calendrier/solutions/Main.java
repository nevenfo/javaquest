void main() {
    int d = Integer.parseInt(IO.readln("Jours ? "));
    int reste = d % 7;
    String nom;
    switch (reste) {
        case 0 -> nom = "Lundi";
        case 1 -> nom = "Mardi";
        case 2 -> nom = "Mercredi";
        case 3 -> nom = "Jeudi";
        case 4 -> nom = "Vendredi";
        case 5 -> nom = "Samedi";
        default -> nom = "Dimanche";
    }
    IO.println((d / 7) + " semaines et " + reste + " jours");
    IO.println("Jour : " + nom);
}
