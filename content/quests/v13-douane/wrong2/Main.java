void main(String[] args) {
    if (args.length < 2) {
        IO.println("Usage : <prix> <quantite>");
        return;
    }
    int total = Integer.parseInt(args[0]) * Integer.parseInt(args[1]);
    IO.println("Total : " + total);
    IO.println("Arrondi bas : " + total);
}
