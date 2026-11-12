public interface Signal {
    String nom();

    int portee();

    default String annonce() {
        return nom() + " : " + portee() + " pas";
    }

    static Signal plusLointain(Signal[] signaux) {
        Signal meilleur = null;
        for (Signal s : signaux) {
            if (meilleur == null || s.portee() > meilleur.portee()) {
                meilleur = s;
            }
        }
        return meilleur;
    }
}
