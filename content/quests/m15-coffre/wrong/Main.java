import java.util.Arrays;

static boolean estMiroir(String s) {
    for (int i = 0; i < s.length() / 2; i++) {
        if (s.charAt(i) != s.charAt(s.length() - 1 - i)) {
            return false;
        }
    }
    return true;
}

static int total(int[] t) {
    int somme = 0;
    for (int v : t) {
        somme += v;
    }
    return somme;
}

static int maximum(int[] t) {
    int max = 0;
    for (int v : t) {
        if (v > max) {
            max = v;
        }
    }
    return max;
}

void main() {
    String mot = IO.readln("Mot du spectre ? ");
    if (!estMiroir(mot)) {
        IO.println("Le spectre reste muet.");
        return;
    }
    int n = Integer.parseInt(IO.readln("Combien de gemmes ? "));
    int[] gemmes = new int[n];
    for (int i = 0; i < n; i++) {
        gemmes[i] = Integer.parseInt(IO.readln("Valeur ? "));
    }
    int somme = total(gemmes);
    int max = maximum(gemmes);
    Arrays.sort(gemmes);
    String ligne = "";
    for (int i = 0; i < n; i++) {
        if (i > 0) {
            ligne = ligne + " ";
        }
        ligne = ligne + gemmes[i];
    }
    IO.println("Triées : " + ligne);
    IO.println("Total : " + somme);
    IO.println("Maximum : " + max);
}
