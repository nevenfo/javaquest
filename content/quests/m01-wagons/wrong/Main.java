void main() {
    int total = 0;
    int wagons = 0;
    int charge = Integer.parseInt(IO.readln("Charge du wagon (0 pour finir) ? "));
    while (charge > 0) {
        total = total + charge;
        wagons++;
        charge = Integer.parseInt(IO.readln("Charge du wagon (0 pour finir) ? "));
    }
    IO.println("Wagons : " + wagons);
    IO.println("Total : " + total);
}
