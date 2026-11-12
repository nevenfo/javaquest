void main() {
    String saisie = IO.readln("Combien de seaux ? ");
    int seaux = Integer.parseInt(saisie);
    int litres = seaux + 12;
    IO.println("Litres : " + litres);
}
