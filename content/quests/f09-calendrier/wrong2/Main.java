void main() {
    int d = Integer.parseInt(IO.readln("Jours ? "));
    int reste = d % 7;
    String[] n = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    IO.println((d / 7) + " semaines et " + reste + " jours");
    IO.println("Jour : " + n[reste]);
}
