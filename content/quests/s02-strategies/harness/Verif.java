import fr.javaquest.runner.Check;

class Verif {
    static void atteint(Strategie s, int pasAttendus, String... grille) {
        Compagnon c = new Compagnon(grille);
        boolean ok = Pilote.conduire(c, s, 100);
        Check.isTrue(ok, "La stratégie n'atteint pas la cible : l'automate s'arrête en (" + c.x() + ", " + c.y() + ") après " + c.pas() + " pas (cible en (" + c.cibleX() + ", " + c.cibleY() + ")).");
        Check.equal(pasAttendus, c.pas(), "Nombre de pas");
    }

    static void verifier() {
        Check.test("Structure : VersCible et LongeMurDroit implémentent Strategie", () -> {
            Check.isTrue(Strategie.class.isAssignableFrom(Check.playerClass("VersCible")), "VersCible doit implémenter Strategie.");
            Check.isTrue(Strategie.class.isAssignableFrom(Check.playerClass("LongeMurDroit")), "LongeMurDroit doit implémenter Strategie.");
        });
        Check.test("VersCible : en diagonale vers le bas-droite", () ->
            atteint(new VersCible(), 7, "C....", ".....", ".....", "....X"));
        Check.test("VersCible : cible en haut à gauche", () ->
            atteint(new VersCible(), 7, "X....", ".....", ".....", "....C"));
        Check.test("VersCible : même colonne, puis même ligne", () -> {
            atteint(new VersCible(), 2, "..C..", ".....", "..X..");
            atteint(new VersCible(), 3, "...X.", ".....", ".....", "...C.");
        });
        Check.test("VersCible : ne déplace pas lui-même l'automate et répond par une direction", () -> {
            Compagnon c = new Compagnon("C..X");
            String d = new VersCible().choisir(c);
            Check.equal(0, c.pas(), "choisir ne doit pas faire avancer l'automate");
            Check.equal("droite", d, "Première direction vers une cible à droite");
        });
        Check.test("LongeMurDroit : couloir en spirale (demi-tours aux culs-de-sac)", () ->
            atteint(new LongeMurDroit(), 20, "C.#...X", "..#.#.#", "..#.#.#", "....#.."));
        Check.test("LongeMurDroit : labyrinthe, main droite sur le mur", () -> {
            atteint(new LongeMurDroit(), 11, "C..#....", ".#....##", ".#.#.#..", "..#.#.#.", ".......X");
            atteint(new LongeMurDroit(), 11, "C.....##", ".#..##..", "..#..##.", ".#.##...", ".......X");
        });
        Check.test("LongeMurDroit : couloirs simples", () -> {
            atteint(new LongeMurDroit(), 8, "C...", "###.", "X...");
            atteint(new LongeMurDroit(), 4, "C...X");
        });
        Check.test("Polymorphisme : les deux stratégies passent par la même interface", () -> {
            Strategie[] toutes = {new VersCible(), new LongeMurDroit()};
            for (Strategie s : toutes) atteint(s, 4, "C...X");
        });
    }
}
