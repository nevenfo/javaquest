import fr.javaquest.runner.Check;
import java.util.*;
class Verif {
    static void verifier() {
        Check.test("Tous les lots sortent une fois", () -> {
            List<String> src = new ArrayList<>(List.of("rose", "bleu", "vert", "or", "noir"));
            Tombola t = new Tombola(new Random(5), src);
            Check.equal(5, t.restants(), "Lots au départ");
            Set<String> sortis = new TreeSet<>();
            for (int i = 0; i < 5; i++) {
                Check.isTrue(sortis.add(t.tirer()), "Un lot est sorti deux fois (tirage sans remise).");
                Check.equal(4 - i, t.restants(), "Lots restants après " + (i + 1) + " tirage(s)");
            }
            Check.equal(new TreeSet<>(src), sortis, "Lots sortis");
        });
        Check.test("La liste du joueur n'est pas modifiée", () -> {
            List<String> src = new ArrayList<>(List.of("a", "b", "c"));
            Tombola t = new Tombola(new Random(1), src);
            t.tirer();
            Check.equal(List.of("a", "b", "c"), src, "Liste d'origine");
            Tombola u = new Tombola(new Random(1), List.of("x", "y"));
            u.tirer();
            Check.equal(1, u.restants(), "Avec une liste non modifiable");
        });
        Check.test("Même graine, même ordre ; graines différentes, ordres variés", () -> {
            List<String> l = List.of("1", "2", "3", "4", "5", "6", "7", "8");
            Set<String> ordres = new HashSet<>();
            for (int s = 0; s < 20; s++) {
                Tombola a = new Tombola(new Random(s), l), b = new Tombola(new Random(s), l);
                StringBuilder oa = new StringBuilder(), ob = new StringBuilder();
                for (int i = 0; i < 8; i++) { oa.append(a.tirer()); ob.append(b.tirer()); }
                Check.equal(oa.toString(), ob.toString(), "Ordre avec la graine " + s + " (deux Random identiques)");
                ordres.add(oa.toString());
            }
            Check.isTrue(ordres.size() > 10, "Les tirages ne varient pas assez : seulement " + ordres.size() + " ordres différents sur 20 graines.");
        });
        Check.test("toString", () -> {
            Tombola t = new Tombola(new Random(3), List.of("rose", "bleu", "vert"));
            Check.equal("Tombola[rose, bleu, vert]", t.toString(), "toString() au départ");
            String sorti = t.tirer();
            var rest = new ArrayList<>(List.of("rose", "bleu", "vert"));
            rest.remove(sorti);
            Check.equal("Tombola" + rest, t.toString(), "toString() après avoir tiré " + sorti);
            Check.equal("Tombola[]", new Tombola(new Random(1), List.of()).toString(), "Tombola vide");
        });
        Check.test("Tombola vide", () -> {
            Tombola t = new Tombola(new Random(1), List.of("seul"));
            Check.equal("seul", t.tirer(), "Dernier lot");
            boolean leve = false;
            try { t.tirer(); } catch (IllegalStateException e) { leve = true; }
            Check.isTrue(leve, "Tirer dans une tombola vide doit lever IllegalStateException.");
        });
    }
}
