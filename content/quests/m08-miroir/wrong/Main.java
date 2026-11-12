void main() {
    String mot = IO.readln("Mot ? ");
    boolean miroir = mot.isEmpty() || mot.charAt(0) == mot.charAt(mot.length() - 1);
    IO.println(miroir ? "oui" : "non");
}
