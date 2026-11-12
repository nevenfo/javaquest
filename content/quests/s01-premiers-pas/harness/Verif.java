import fr.javaquest.runner.Check;

class Verif {
    static void cas(String nom, int pasAttendus, String... grille) {
        Check.test(nom, () -> {
            Compagnon c = new Compagnon(grille);
            Main.guider(c);
            Check.isTrue(c.surCible(), "L'automate doit finir sur la cible : il est en (" + c.x() + ", " + c.y() + "), la cible est en (" + c.cibleX() + ", " + c.cibleY() + ").");
            Check.equal(pasAttendus, c.pas(), "Nombre de pas (aucun détour)");
            Check.equal(java.util.List.of("Arrivé !"), c.dits(), "Ce que dit l'automate");
        });
    }

    static void verifier() {
        cas("Couloir, mur à droite, cible deux rangs plus bas", 6,
            "C....#.",
            ".#...#.",
            "....X#.",
            ".......");
        cas("Bord de la grille à la place du mur", 7,
            "C.....",
            "......",
            ".....X",
            "......");
        cas("Limite : la cible est au bout de la première ligne (aucune descente)", 3,
            "C..X",
            "....",
            "....");
        cas("Limite : grille d'une seule colonne (aucun pas à droite)", 2,
            "C",
            ".",
            "X",
            ".");
        cas("Mur proche : un seul pas à droite, puis descente", 3,
            "C.#...",
            ".....#",
            ".X.#..",
            "......",
            "......");
    }
}
