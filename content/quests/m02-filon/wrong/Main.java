void main() {
    int volume = Integer.parseInt(IO.readln("Volume de départ ? "));
    int objectif = Integer.parseInt(IO.readln("Objectif ? "));
    int jours = 0;
    while (volume <= objectif) {
        volume = volume * 2;
        jours++;
    }
    IO.println("Jours : " + jours);
    IO.println("Volume : " + volume);
}
