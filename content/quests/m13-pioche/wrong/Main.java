static int degats(int base, int niveau) {
    return base + niveau;
}

static boolean estSolide(int durete) {
    return durete >= 7;
}

void main() {
    IO.println("Dégâts : " + degats(4, 3));
}
