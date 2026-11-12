import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Mission mission = new Mission(new ParAxes());
        mission.ajouter(new Etape("Aube", 3, 0));
        mission.ajouter(new Etape("Zénith", 3, 2));
        mission.ajouter(new Etape("Crépuscule", 7, 2));
        try {
            Map<String, Integer> bilan = mission.executer(Compagnon.demarrer());
            for (Map.Entry<String, Integer> e : bilan.entrySet()) IO.println(e.getKey() + " : " + e.getValue() + " pas");
        } catch (MissionException e) {
            IO.println("Mission interrompue : " + e.getMessage());
        }
    }
}
