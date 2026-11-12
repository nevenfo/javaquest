void main() {
    String[] runes = {"4", "x", "7", "", "2", "9"};
    int total = 0;
    for (int i = 0; i < runes.length; i++) {
        try {
            int v = Integer.parseInt(runes[i]);
            switch (v % 3) {
                case 0:
                    IO.println("pierre " + v);
                case 1:
                    IO.println("feu " + v);
                    break;
                default:
                    IO.println("eau " + v);
            }
            total += v;
        } catch (NumberFormatException e) {
            IO.println("rune brouillée n°" + i);
        }
    }
    IO.println("Bilan : " + total);
}
