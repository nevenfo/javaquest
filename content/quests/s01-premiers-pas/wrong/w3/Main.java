public class Main {
    static void guider(Compagnon c) {
        while (!c.devantMur("droite")) {
            c.avancer("droite");
        }
        while (!c.surCible()) {
            c.avancer("bas");
        }
    }

    public static void main(String[] args) {
        guider(Compagnon.demarrer());
    }
}
