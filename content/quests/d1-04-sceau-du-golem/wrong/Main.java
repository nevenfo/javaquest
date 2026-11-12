static int[] compter(String s) {
    int[] t = new int[26];
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= 'a' && c <= 'z') {
            t[c - 'a']++;
        }
    }
    return t;
}

static int indexMax(int[] t) {
    int best = 0;
    for (int i = 1; i < t.length; i++) {
        if (t[i] >= t[best]) {
            best = i;
        }
    }
    return best;
}

void main() {
    String texte = IO.readln("Inscription du golem ? ");
    int[] t = compter(texte);
    int i = indexMax(t);
    if (t[i] == 0) {
        IO.println("Le golem ne lit rien.");
    } else {
        IO.println("Lettre reine : " + (char) ('a' + i));
        IO.println("Occurrences : " + t[i]);
    }
}
