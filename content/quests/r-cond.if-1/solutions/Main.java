void main() {
    int jet = Integer.parseInt(IO.readln("Jet ? "));
    if (jet == 1) {
        IO.println("Échec critique");
    } else if (jet >= 18) {
        IO.println("Réussite critique");
    } else if (jet >= 10) {
        IO.println("Réussite");
    } else {
        IO.println("Échec");
    }
}
