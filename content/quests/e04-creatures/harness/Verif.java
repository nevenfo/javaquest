import fr.javaquest.runner.Check;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

class Verif {
    static String sortie(Runnable r) {
        PrintStream avant = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true, java.nio.charset.StandardCharsets.UTF_8));
        try { r.run(); } finally { System.setOut(avant); }
        return buf.toString(java.nio.charset.StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
    }

    static void capacites(String classe, boolean marche, boolean nage, boolean vole) {
        Class<?> c = Check.playerClass(classe);
        Check.equal(marche, Marcheur.class.isAssignableFrom(c), classe + " est-il un Marcheur");
        Check.equal(nage, Nageur.class.isAssignableFrom(c), classe + " est-il un Nageur");
        Check.equal(vole, Volant.class.isAssignableFrom(c), classe + " est-il Volant");
    }

    static void verifier() {
        Check.test("Chaque créature a exactement ses capacités", () -> {
            capacites("Chat", true, false, false);
            capacites("Saumon", false, true, false);
            capacites("Corbeau", true, false, true);
            capacites("Cygne", true, true, true);
        });
        Check.test("Les créatures se manipulent par leurs interfaces", () -> {
            Marcheur m = new Chat("Mina");
            Check.equal("Mina marche 3 pas", sortie(() -> m.marcher(3)), "Chat.marcher");
            Nageur n = new Saumon("Bleu");
            Check.equal("Bleu nage 12 brasses", sortie(() -> n.nager(12)), "Saumon.nager");
            Volant v = new Corbeau("Noiraud");
            Check.equal("Noiraud vole 40 battements", sortie(() -> v.voler(40)), "Corbeau.voler");
        });
        Check.test("Le cygne combine les trois capacités", () -> {
            Cygne c = new Cygne("Odette");
            Check.equal("Odette marche 1 pas\nOdette nage 2 brasses\nOdette vole 0 battements",
                sortie(() -> { c.marcher(1); c.nager(2); c.voler(0); }), "Cygne");
        });
        Check.test("Polymorphisme : une liste de marcheurs hétérogène", () -> {
            List<Marcheur> l = List.of(new Chat("A"), new Corbeau("B"), new Cygne("C"));
            Check.equal("A marche 5 pas\nB marche 5 pas\nC marche 5 pas", sortie(() -> { for (Marcheur x : l) x.marcher(5); }), "parcours");
        });
    }
}
