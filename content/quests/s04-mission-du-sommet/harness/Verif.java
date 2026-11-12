import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static Mission mission(Etape... etapes) {
        Mission m = new Mission(new ParAxes());
        for (Etape e : etapes) m.ajouter(e);
        return m;
    }

    static void verifier() {
        Check.test("Structure : record, interface, exception vérifiée", () -> {
            Check.isTrue(Check.playerClass("Etape").isRecord(), "Etape doit être un record.");
            Check.isTrue(Check.playerClass("Trajet").isInterface(), "Trajet doit être une interface.");
            Class<?> ex = Check.playerClass("MissionException");
            Check.isTrue(Exception.class.isAssignableFrom(ex) && !RuntimeException.class.isAssignableFrom(ex), "MissionException doit hériter de Exception (exception vérifiée), pas de RuntimeException.");
            Check.isTrue(Trajet.class.isAssignableFrom(Check.playerClass("ParAxes")), "ParAxes doit implémenter Trajet.");
        });
        Check.test("Etape : valeur, égalité, validation", () -> {
            Etape e = new Etape("Aube", 3, 1);
            Check.equal("Aube", e.nom(), "nom");
            Check.equal(3, e.x(), "x");
            Check.equal(1, e.y(), "y");
            Check.equal(new Etape("Aube", 3, 1), e, "deux étapes identiques sont égales");
            Check.equal(new Etape("Z", 0, 0), new Etape("Z", 0, 0), "coordonnées nulles autorisées");
            Object[][] mauvais = {{"", 0, 0}, {"  ", 0, 0}, {null, 0, 0}, {"A", -1, 0}, {"A", 0, -1}};
            for (Object[] m : mauvais) {
                try {
                    new Etape((String) m[0], (Integer) m[1], (Integer) m[2]);
                    Check.fail("Etape(" + m[0] + ", " + m[1] + ", " + m[2] + ") devrait lever IllegalArgumentException.");
                } catch (IllegalArgumentException ok) { }
            }
        });
        Check.test("ParAxes : d'abord l'axe x, puis l'axe y", () -> {
            Compagnon c = new Compagnon("C..", "#..", "...");
            new ParAxes().rejoindre(c, new Etape("Coin", 2, 2));
            Check.equal(2, c.x(), "colonne");
            Check.equal(2, c.y(), "ligne");
            Check.equal(4, c.pas(), "nombre de pas");
            new ParAxes().rejoindre(c, new Etape("Retour", 2, 0));
            Check.equal(6, c.pas(), "retour vers le haut");
        });
        Check.test("Mission : collection d'étapes, copie défensive, pas de doublon de nom", () -> {
            Mission m = mission(new Etape("A", 1, 0), new Etape("B", 2, 0));
            Check.equal(List.of(new Etape("A", 1, 0), new Etape("B", 2, 0)), m.etapes(), "étapes dans l'ordre d'ajout");
            try { m.etapes().add(new Etape("Z", 0, 0)); } catch (UnsupportedOperationException ok) { }
            Check.equal(2, m.etapes().size(), "modifier la liste renvoyée ne doit pas changer la mission");
            try {
                m.ajouter(new Etape("A", 5, 5));
                Check.fail("Une étape portant déjà le nom « A » doit être refusée (IllegalArgumentException).");
            } catch (IllegalArgumentException ok) { }
            Check.equal(2, m.etapes().size(), "l'étape refusée n'est pas ajoutée");
        });
        Check.test("executer : bilan des pas, dans l'ordre des étapes", () -> {
            Compagnon c = new Compagnon("C.....", "......", "......", "......");
            Map<String, Integer> bilan = mission(new Etape("Zéta", 3, 0), new Etape("Alpha", 3, 2), new Etape("Mu", 0, 2)).executer(c);
            Check.equal(List.of("Zéta", "Alpha", "Mu"), new ArrayList<>(bilan.keySet()), "ordre du bilan (celui des étapes, pas alphabétique)");
            Check.equal(List.of(3, 2, 3), new ArrayList<>(bilan.values()), "pas par étape");
            Check.equal(List.of("Étape Zéta", "Étape Alpha", "Étape Mu"), c.dits(), "l'automate annonce chaque étape atteinte");
            Check.equal(8, c.pas(), "pas au total");
        });
        Check.test("executer : limites (mission vide, étape sur place)", () -> {
            Compagnon c = new Compagnon("C...");
            Check.equal(Map.of(), mission().executer(c), "mission vide : bilan vide");
            Check.equal(0, c.pas(), "mission vide : aucun pas");
            Check.equal(List.of(), c.dits(), "mission vide : rien à dire");
            Map<String, Integer> b = mission(new Etape("Ici", 0, 0)).executer(c);
            Check.equal(Map.of("Ici", 0), b, "étape sur place : 0 pas");
            Check.equal(List.of("Étape Ici"), c.dits(), "étape sur place : annoncée quand même");
        });
        Check.test("executer : un mur devient une MissionException", () -> {
            Compagnon c = new Compagnon("C.#...", "......");
            Mission m = mission(new Etape("Proche", 1, 0), new Etape("Mur", 4, 0));
            try {
                m.executer(c);
                Check.fail("Le mur de la case (2, 0) doit interrompre la mission par une MissionException.");
            } catch (MissionException e) {
                Check.isTrue(e.getMessage() != null && e.getMessage().contains("Mur"), "Le message doit citer l'étape « Mur » (reçu : " + e.getMessage() + ").");
                Check.isTrue(e.getCause() instanceof Compagnon.MurException, "La cause doit être la MurException de l'automate.");
            }
            Check.equal(1, c.x(), "l'automate reste devant le mur");
            Check.equal(List.of("Étape Proche"), c.dits(), "seule l'étape atteinte a été annoncée");
        });
        Check.test("executer : étape hors de la grille", () -> {
            Compagnon c = new Compagnon("C...");
            try {
                mission(new Etape("Loin", 9, 0)).executer(c);
                Check.fail("Une étape hors de la grille doit lever MissionException.");
            } catch (MissionException e) {
                Check.equal(3, c.x(), "l'automate va jusqu'au bord");
            }
        });
        Check.test("Trajet : l'architecture accepte une autre stratégie de déplacement", () -> {
            Compagnon c = new Compagnon("C...", "....", "....");
            Mission m = new Mission((cp, e) -> { cp.avancer("bas"); cp.avancer("bas"); });
            m.ajouter(new Etape("Plongeon", 0, 2));
            Check.equal(Map.of("Plongeon", 2), m.executer(c), "bilan avec un trajet personnalisé (les pas sont comptés sur l'automate)");
            Mission n = new Mission((cp, e) -> { throw new MissionException("sabotage", null); });
            n.ajouter(new Etape("X", 1, 1));
            try {
                n.executer(new Compagnon("C.."));
                Check.fail("La MissionException d'un trajet doit traverser executer.");
            } catch (MissionException e) {
                Check.equal("sabotage", e.getMessage(), "message conservé");
            }
        });
        Check.test("Main : mission de démonstration sur la grille d'essai", () -> {
            Check.equal(List.of("Aube : 3 pas", "Zénith : 2 pas", "Crépuscule : 4 pas"), Check.lines(Check.runMain("Main", "")), "sortie de main");
        });
    }
}
