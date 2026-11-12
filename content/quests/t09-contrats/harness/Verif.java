
import fr.javaquest.runner.Check;
import java.lang.reflect.*;
import java.util.*;

class Verif {
    static Contrat c(String t, int v) {
        return new Contrat() {
            public String titre() { return t; }
            public int valeur() { return v; }
        };
    }
    static String noms(List<Contrat> l) {
        StringBuilder sb = new StringBuilder();
        for (Contrat x : l) sb.append(x.titre()).append(' ');
        return sb.toString().trim();
    }
    static void verifier() {
        Check.test("Structure de Contrat", () -> {
            Class<?> k = Check.playerClass("Contrat");
            Check.isTrue(k.isInterface() && Comparable.class.isAssignableFrom(k), "Contrat doit être une interface qui étend Comparable<Contrat>.");
            Check.isTrue(k.getMethod("resume").isDefault(), "resume() doit être default.");
            Check.isTrue(k.getMethod("compareTo", Contrat.class).isDefault(), "compareTo(Contrat) doit être default.");
            Check.isTrue(Modifier.isStatic(k.getMethod("creer", String.class, int.class).getModifiers()), "creer doit être static.");
        });
        Check.test("resume et creer", () -> {
            Check.equal("Pacte : 40", c("Pacte", 40).resume(), "resume()");
            Contrat k = Contrat.creer("Traité", 7);
            Check.equal("Traité", k.titre(), "titre()");
            Check.equal(7, k.valeur(), "valeur()");
            Check.equal("Traité : 7", k.resume(), "resume() d'un contrat fabriqué");
        });
        Check.test("compareTo par défaut", () -> {
            Check.isTrue(c("A", 10).compareTo(c("B", 5)) < 0, "Le contrat le plus précieux passe avant.");
            Check.isTrue(c("A", 5).compareTo(c("B", 10)) > 0, "Le moins précieux passe après.");
            Check.isTrue(c("A", 5).compareTo(c("B", 5)) < 0, "À valeur égale, ordre alphabétique des titres.");
            Check.equal(0, c("A", 5).compareTo(c("A", 5)), "Même titre, même valeur");
            Check.isTrue(c("A", Integer.MAX_VALUE).compareTo(c("B", Integer.MIN_VALUE)) < 0, "Valeurs extrêmes : pas de débordement.");
        });
        Check.test("classer", () -> {
            List<Contrat> src = new ArrayList<>(List.of(c("pacte", 5), c("alliance", 9), c("bail", 5), Contrat.creer("zeste", 1), c("accord", 5)));
            List<Contrat> r = Registre.classer(src);
            Check.equal("alliance accord bail pacte zeste", noms(r), "Contrats classés");
            Check.equal("pacte alliance bail zeste accord", noms(src), "La liste d'origine ne doit pas changer");
        });
        Check.test("classer : cas limites", () -> {
            Check.equal(0, Registre.classer(new ArrayList<>()).size(), "Liste vide");
            Check.equal("seul", noms(Registre.classer(new ArrayList<>(List.of(c("seul", 3))))), "Un seul contrat");
        });
        Check.test("meilleur", () -> {
            Check.equal("alliance", Registre.meilleur(List.of(c("pacte", 5), c("alliance", 9), c("bail", 9))).titre(), "meilleur (égalité : ordre alphabétique)");
            Check.isTrue(Registre.meilleur(List.of()) == null, "Liste vide : null.");
        });
    }
}
