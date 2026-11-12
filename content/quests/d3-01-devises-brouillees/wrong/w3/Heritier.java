public class Heritier extends Ancetre implements Comparable<Heritier> {
    private final String vertu;

    public Heritier(String nom, int annee, String vertu) {
        super(nom, annee);
        this.vertu = vertu;
    }

    @Override
    public String devise() {
        return "Honneur et " + vertu;
    }

    @Override
    public int compareTo(Heritier autre) {
        if (annee == autre.annee) {
            return nom.compareTo(autre.nom);
        }
        return annee - autre.annee;
    }
}
