void main() {
    int n = 1;
    switch (n) {
        case 1:
            IO.print("A");
        case 2:
            IO.print("B");
            break;
        case 3:
            IO.print("C");
        default:
            IO.print("D");
    }
    IO.println();
    n = 3;
    switch (n) {
        case 1:
            IO.print("A");
        case 2:
            IO.print("B");
            break;
        case 3:
            IO.print("C");
        default:
            IO.print("D");
    }
    IO.println();
    String mot = "feu";
    switch (mot) {
        case "eau" -> IO.println("bleu");
        case "feu" -> IO.println("rouge");
        default -> IO.println("?");
    }
}
