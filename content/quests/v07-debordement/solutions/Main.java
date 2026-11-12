
void main() {
    int jour = Integer.parseInt(IO.readln("Quel jour ? "));
    long pieces = 1;
    for (int i = 1; i < jour; i++) {
        pieces = pieces * 2;
    }
    IO.println("Jour " + jour + " : " + pieces + " pièces");
}
