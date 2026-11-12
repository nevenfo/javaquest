import fr.javaquest.runner.Check;

class Verif {
    static void essai(String mot, boolean doitPasser) {
        Check.test("Mot « " + mot + " »", () -> {
            Pont.reinitialiser();
            Check.runMain("Main", mot + "\n");
            if (doitPasser) {
                Check.equal(1, Pont.passages(), "Nombre d'appels à laisserPasser()");
                Check.equal(0, Pont.refus(), "Nombre d'appels à refuser()");
            } else {
                Check.equal(1, Pont.refus(), "Nombre d'appels à refuser() pour « " + mot + " »");
                Check.equal(0, Pont.passages(), "Nombre d'appels à laisserPasser() pour « " + mot + " »");
            }
        });
    }

    static void verifier() {
        essai("pomme-de-pin", true);
        essai("Pomme-de-pin", false);
        essai("pomme", false);
        essai("", false);
        essai("pomme-de-pins", false);
    }
}
