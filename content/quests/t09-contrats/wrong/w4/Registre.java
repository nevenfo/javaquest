import java.util.*;

public class Registre {
    static List<Contrat> classer(List<Contrat> contrats) {
        List<Contrat> copie = new ArrayList<>(contrats);
        Collections.sort(copie);
        return copie;
    }

    static Contrat meilleur(List<Contrat> contrats) {
        List<Contrat> t = classer(contrats);
        return t.isEmpty() ? null : t.get(0);
    }
}
