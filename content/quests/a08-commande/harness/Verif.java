
import fr.javaquest.runner.Check;
import java.lang.reflect.*;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Commande est un record avec les bonnes composantes", () -> {
            Class<?> c = Check.playerClass("Commande");
            Check.isTrue(c.isRecord(), "Commande doit être déclarée avec `record`.");
            RecordComponent[] rc = c.getRecordComponents();
            Check.equal(3, rc.length, "Nombre de composantes");
            Check.equal("client", rc[0].getName(), "Première composante");
            Check.equal("article", rc[1].getName(), "Deuxième composante");
            Check.equal("quantite", rc[2].getName(), "Troisième composante");
            Check.equal(int.class, rc[2].getType(), "Type de quantite");
        });
        Check.test("Accesseurs et toString fournis par le record", () -> {
            Commande c = new Commande("Ana", "clou", 3);
            Check.equal("Ana", c.client(), "client()");
            Check.equal("clou", c.article(), "article()");
            Check.equal(3, c.quantite(), "quantite()");
            Check.equal("Commande[client=Ana, article=clou, quantite=3]", c.toString(), "toString()");
        });
        Check.test("equals et HashSet", () -> {
            Set<Commande> s = new HashSet<>(List.of(new Commande("A", "x", 1), new Commande("B", "x", 1)));
            s.add(new Commande("A", "x", 1));
            Check.equal(2, s.size(), "Commandes distinctes");
        });
        int[] qs = {0, 1, 9, 10, 11, 500};
        boolean[] att = {false, false, false, true, true, true};
        for (int i = 0; i < qs.length; i++) {
            int q = qs[i]; boolean a = att[i];
            Check.test("estGrosse pour une quantité de " + q, () ->
                Check.equal(a, new Commande("C", "arc", q).estGrosse(), "estGrosse()"));
        }
        Check.test("Fabrique unique", () -> {
            Commande c = Commande.unique("Zoé", "bouclier");
            Check.equal(new Commande("Zoé", "bouclier", 1), c, "Commande.unique(\"Zoé\", \"bouclier\")");
            Check.isTrue(!c.estGrosse(), "Une commande unique n'est pas grosse.");
        });
    }
}
