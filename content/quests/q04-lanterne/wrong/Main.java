void main() {
    int sacs = Integer.parseInt(IO.readln("Combien de sacs ? "));
    IO.println("Caisses : " + sacs / 10.0);
    IO.println("Sacs restants : " + sacs % 10);
}
