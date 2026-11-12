void main() {
    int n = Integer.parseInt(IO.readln("Longueur du couloir ? "));
    for (int i = 1; i <= n; i++) {
        Golem.avancer();
        Golem.creuser();
        if (i % 3 == 0) {
            Golem.poserPierre();
        }
    }
}
