public interface Contrat extends Comparable<Contrat> {
    String titre();
    int valeur();

    default int compareTo(Contrat autre) {
        int c = autre.valeur() - valeur();
        return c != 0 ? c : titre().compareTo(autre.titre());
    }

    default String resume() {
        return titre() + " : " + valeur();
    }

    static Contrat creer(String titre, int valeur) {
        return new Contrat() {
            public String titre() { return titre; }
            public int valeur() { return valeur; }
        };
    }
}
