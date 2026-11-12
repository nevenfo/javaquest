public class Main {
    public static void main(String[] args) {
        Animal[] zoo = { new Canard("Donald"), new Grenouille("Kermit"), new Poisson("Nemo") };
        String[] acteurs = { "canard", "grenouille", "poisson" };
        for (int i = 0; i < zoo.length; i++) {
            Scene.presenter(acteurs[i], zoo[i].getNom(), zoo[i]);
        }
    }
}
