void main() {
    String direction = IO.readln("Direction ? ");
    String panneau = switch (direction) {
        case "nord" -> "Vers la Tour";
        case "sud" -> "Vers Javelune";
        case "est" -> "Vers les Mines de Boucle-Profonde";
        case "ouest" -> "Vers la mer";
        default -> "Vers les Mines de Boucle-Profonde";
    };
    IO.println(panneau);
}
