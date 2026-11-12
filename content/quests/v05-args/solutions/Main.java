
void main(String[] args) {
    if (args.length == 0) {
        IO.println("Halte ! Donne ton nom.");
        return;
    }
    IO.println("Bienvenue, " + args[0] + " !");
    if (args.length >= 2) {
        int jours = Integer.parseInt(args[1]);
        IO.println("Séjour : " + jours * 24 + " heures");
    }
}
