static int degats(int base, int niveau) {
    return base + 2 * niveau;
}

static boolean estSolide(int durete) {
    return durete >= 7;
}

void main() {
    IO.println("Dégâts : " + degats(4, 3));
    IO.println("Solide : " + estSolide(5));
}
