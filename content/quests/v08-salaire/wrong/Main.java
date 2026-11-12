
void main() {
    int minutes = Integer.parseInt(IO.readln("Minutes ? "));
    double tarif = Double.parseDouble(IO.readln("Tarif ? "));
    IO.println("Heures : " + minutes / 60);
    IO.println("Minutes : " + minutes % 60);
    IO.println("Salaire : " + minutes / 60 * tarif);
}
