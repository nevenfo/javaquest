import java.util.*;

public class Mission {
    private final Trajet trajet;
    private final List<Etape> etapes = new ArrayList<>();

    public Mission(Trajet trajet) {
        this.trajet = trajet;
    }

    public void ajouter(Etape e) {
        for (Etape existante : etapes) {
            if (existante.nom().equals(e.nom())) throw new IllegalArgumentException("Étape déjà prévue : " + e.nom());
        }
        etapes.add(e);
    }

    public List<Etape> etapes() {
        return List.copyOf(etapes);
    }

    public Map<String, Integer> executer(Compagnon c) throws MissionException {
        Map<String, Integer> bilan = new LinkedHashMap<>();
        for (Etape e : etapes) {
            int avant = c.pas();
            try {
                trajet.rejoindre(c, e);
            } catch (Compagnon.MurException ex) {
                throw new MissionException("Étape « " + e.nom() + " » inaccessible : " + ex.getMessage(), ex);
            }
            c.dire("Étape " + e.nom());
            bilan.put(e.nom(), c.pas() - avant);
        }
        return bilan;
    }
}
