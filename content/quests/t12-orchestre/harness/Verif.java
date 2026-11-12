
import fr.javaquest.runner.Check;
import java.util.*;

class Verif {
    static void verifier() {
        Check.test("Tambour et flûtes", () -> {
            Check.equal("Tambour : boum", new Tambour().jouer(), "Tambour.jouer()");
            Check.equal("Flûte : tiii", new Flute(true).jouer(), "Flute(true).jouer()");
            Check.equal("Flûte : fouuu", new Flute(false).jouer(), "Flute(false).jouer()");
        });
        Check.test("Violon : redéfinition avec super", () -> {
            Instrument i = new Violon(3);
            Check.equal("Violon : vivivi (archet)", i.jouer(), "Violon(3).jouer()");
            Check.equal("Violon : vi (archet)", new Violon(1).jouer(), "Violon(1).jouer()");
            Check.equal("vivi", new Violon(2).son(), "Violon(2).son()");
        });
        Check.test("Hiérarchie", () -> {
            for (Class<?> c : List.of(Tambour.class, Flute.class, Violon.class))
                Check.isTrue(c.getSuperclass() == Instrument.class, c.getSimpleName() + " doit étendre Instrument.");
        });
        Check.test("Main.concert", () -> {
            Check.equal("Tambour : boum | Flûte : tiii | Violon : vivi (archet)", Main.concert(List.of(new Tambour(), new Flute(true), new Violon(2))), "concert à trois");
            Check.equal("Tambour : boum", Main.concert(List.of(new Tambour())), "concert d'un seul instrument");
            Check.equal("", Main.concert(new ArrayList<>()), "concert vide");
        });
    }
}
