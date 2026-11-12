public class Main {
    static int[] position(String ligne) {
        String[] parties = ligne.split(":");
        String[] xy = parties[0].split(",");
        return new int[] { Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim()) };
    }

    static String objet(String ligne) {
        return ligne.split(":")[1];
    }
}
