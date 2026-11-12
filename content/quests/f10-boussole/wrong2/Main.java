void main(String[] args) {
    if (args.length < 2) {
        IO.println("Usage : <direction> <distance>");
        return;
    }
    int d = Integer.parseInt(args[1]);
    int x = 0;
    int y = 0;
    switch (args[0]) {
        case "nord" -> y += d;
        case "sud" -> y -= d;
        case "est" -> x += d;
        case "ouest" -> x -= d;
        default -> {
            IO.println("Direction inconnue : " + args[0]);
            return;
        }
    }
    IO.println("Position : (" + x + ", " + y + ")");
}
