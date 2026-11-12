
void main(String[] args) {
    IO.println("Bienvenue, " + args[0] + " !");
    if (args.length >= 2) {
        IO.println("Séjour : " + Integer.parseInt(args[1]) * 24 + " heures");
    }
}
