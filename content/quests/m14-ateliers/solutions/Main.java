static long puissance(int base, int exp) {
    long resultat = 1;
    for (int i = 0; i < exp; i++) {
        resultat = resultat * base;
    }
    return resultat;
}

static boolean estPremier(int n) {
    if (n < 2) {
        return false;
    }
    for (int d = 2; d * d <= n; d++) {
        if (n % d == 0) {
            return false;
        }
    }
    return true;
}

static int occurrences(String mot, char lettre) {
    int compte = 0;
    for (int i = 0; i < mot.length(); i++) {
        if (mot.charAt(i) == lettre) {
            compte++;
        }
    }
    return compte;
}

void main() {
}
