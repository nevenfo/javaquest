void main(String[] args) {
    if (args.length > 1) {
        IO.println("Salut, " + args[0] + " !");
    } else {
        IO.println("Salut, inconnu !");
    }
    IO.println("Arguments reçus : " + args.length);
}
