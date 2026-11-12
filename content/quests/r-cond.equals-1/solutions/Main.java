void main() {
    String a = IO.readln("Premier mot ? ");
    String b = IO.readln("Second mot ? ");
    if (a.equals(b)) {
        IO.println("Identiques");
    } else {
        IO.println("Différents");
    }
}
