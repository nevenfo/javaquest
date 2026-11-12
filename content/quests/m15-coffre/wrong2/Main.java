static boolean estMiroir(String s) {
    return s.isEmpty() || s.charAt(0) == s.charAt(s.length() - 1);
}

static int total(int[] t) {
    int somme = 0;
    for (int v : t) {
        somme += v;
    }
    return somme;
}

static int maximum(int[] t) {
    int max = t[0];
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
    java.util.Arrays.sort(gemmes);
    String ligne = "";
    for (int i = 0; i < n; i++) {
        ligne = ligne + (i > 0 ? " " : "") + gemmes[i];
    }
    IO.println("Triées : " + ligne);
    IO.println("Total : " + total(gemmes));
    IO.println("Maximum : " + maximum(gemmes));
}
