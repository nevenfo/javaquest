void main() {
    int n = Integer.parseInt(IO.readln("Longueur du couloir ? "));
    for (int i = 0; i < n; i++) {
        Golem.avancer();
        Golem.creuser();
        if (i % 3 == 0) {
            Golem.poserPierre();
        }
    }
}
