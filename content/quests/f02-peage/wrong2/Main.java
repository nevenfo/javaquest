void main() {
    int age = Integer.parseInt(IO.readln("Âge ? "));
    if (age < 6) {
        IO.println("Passage gratuit");
    } else if (age < 18) {
        IO.println("Tarif enfant : 2 pièces");
    } else if (age < 65) {
        IO.println("Tarif normal : 5 pièces");
    } else {
        IO.println("Tarif aîné : 3 pièces");
    }
}
