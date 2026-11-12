void main() {
    int d = Integer.parseInt(IO.readln("Jours ? "));
    double sem = d / 7.0;
    int reste = d % 7;
    String[] n = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
    IO.println(sem + " semaines et " + reste + " jours");
    IO.println("Jour : " + n[reste]);
}
