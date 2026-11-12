import java.util.LinkedHashMap;
import java.util.Map;

public class Chaudron {
    private final int capacite;
    private final Map<String, Integer> ingredients = new LinkedHashMap<>();
    private int contenu;

    public Chaudron(int capacite) {
        if (capacite <= 0) throw new IllegalArgumentException("capacité invalide : " + capacite);
        this.capacite = capacite;
    }

    public void verser(String ingredient, int doses) {
        if (doses <= 0) throw new IllegalArgumentException("doses invalides : " + doses);
        if (contenu + doses > capacite) throw new IllegalStateException("le chaudron déborde");
        ingredients.merge(ingredient, doses, Integer::sum);
        contenu += doses;
    }

    public int contenu() { return contenu; }

    public int doses(String ingredient) { return ingredients.getOrDefault(ingredient, 0); }

    public String preparer() {
        if (contenu == 0) throw new IllegalStateException("chaudron vide");
        String meilleur = null;
        for (var e : ingredients.entrySet())
            if (meilleur == null || e.getValue() > ingredients.get(meilleur)) meilleur = e.getKey();
        ingredients.clear();
        contenu = 0;
        return "Potion de " + meilleur;
    }
}
