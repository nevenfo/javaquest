void main(String[] args) {
    if (args.length < 2) {
        IO.println("Usage : <prix> <quantite>");
        return;
    }
    double prix = Double.parseDouble(args[0]);
    int quantite = Integer.parseInt(args[1]);
    double total = prix * quantite;
    IO.println("Total : " + total);
    IO.println("Arrondi bas : " + (int) total);
}
