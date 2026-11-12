void main() {
    int n = Integer.parseInt(IO.readln("Combien de cristaux ? "));
    int[] compteurs = new int[4];
    for (int i = 0; i < n; i++) {
        int couleur = Integer.parseInt(IO.readln("Couleur ? "));
        compteurs[couleur]++;
    }
    for (int c = 0; c < compteurs.length; c++) {
        IO.println(c + " : " + compteurs[c]);
    }
}
