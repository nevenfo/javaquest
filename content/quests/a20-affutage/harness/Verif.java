import fr.javaquest.runner.Check;

class Verif {
    static void verifier() {
        Check.test("affuter augmente le tranchant", () -> {
            Lame l = new Lame("Aube", 50);
            l.affuter(30);
            Check.equal(80, l.getTranchant(), "Tranchant après affûtage de 30");
            l.affuter(5);
            Check.equal(85, l.getTranchant(), "Tranchant après un second affûtage");
        });
        Check.test("Le tranchant plafonne à 100", () -> {
            Lame l = new Lame("Crépuscule", 90);
            l.affuter(50);
            Check.equal(100, l.getTranchant(), "Tranchant plafonné");
        });
        Check.test("estAffutee", () -> {
            Lame l = new Lame("Aube", 80);
            Check.isTrue(l.estAffutee(80), "Tranchant 80 et seuil 80 : la lame est affûtée (au moins égal).");
            Check.isTrue(l.estAffutee(10), "Tranchant 80 et seuil 10.");
            Check.isTrue(!l.estAffutee(81), "Tranchant 80 et seuil 81 : pas assez.");
        });
    }
}
