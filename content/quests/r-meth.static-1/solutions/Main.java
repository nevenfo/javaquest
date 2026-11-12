static String grade(int xp) {
    if (xp < 100) {
        return "Novice";
    } else if (xp < 500) {
        return "Adepte";
    }
    return "Maître";
}

static int ecart(int a, int b) {
    if (a > b) {
        return a - b;
    }
    return b - a;
}

void main() {
}
