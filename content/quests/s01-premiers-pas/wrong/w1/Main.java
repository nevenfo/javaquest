public class Main {
    static void guider(Compagnon c) {
        for (int i = 0; i < 4; i++) {
            c.avancer("droite");
        }
        while (!c.surCible()) {
            c.avancer("bas");
        }
        c.dire("Arrivé !");
    }

    public static void main(String[] args) {
        guider(Compagnon.demarrer());
    }
}
