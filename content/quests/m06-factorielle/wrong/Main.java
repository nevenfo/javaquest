void main() {
    int n = Integer.parseInt(IO.readln("n ? "));
    int resultat = 1;
    for (int i = 2; i <= n; i++) {
        resultat = resultat * i;
    }
    IO.println(n + "! = " + resultat);
}
