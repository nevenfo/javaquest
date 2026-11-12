void main() {
    int jet = Integer.parseInt(IO.readln("Jet ? "));
    if (jet < 10) {
        IO.println("Échec");
    } else if (jet == 1) {
        IO.println("Échec critique");
    } else if (jet >= 18) {
        IO.println("Réussite critique");
    } else {
        IO.println("Réussite");
    }
}
