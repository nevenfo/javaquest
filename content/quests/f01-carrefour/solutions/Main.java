void main() {
    int pv = Integer.parseInt(IO.readln("PV du voyageur ? "));
    if (pv >= 15) {
        IO.println("Voyageur en forme");
    } else if (pv >= 5) {
        IO.println("Voyageur blessé : tisane");
    } else {
        IO.println("Voyageur en danger : soins urgents");
    }
}
