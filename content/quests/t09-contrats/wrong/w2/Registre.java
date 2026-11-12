import java.util.*;

public class Registre {
    static List<Contrat> classer(List<Contrat> contrats) {
        Collections.sort(contrats);
        return contrats;
    }

    static Contrat meilleur(List<Contrat> contrats) {
        List<Contrat> t = classer(contrats);
        return t.isEmpty() ? null : t.get(0);
    }
}
