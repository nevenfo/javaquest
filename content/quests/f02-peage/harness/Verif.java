import fr.javaquest.runner.Check;

class Verif {
    static String tarif(int age) {
        if (age < 0 || age > 120) return "Âge invalide";
        if (age < 6) return "Passage gratuit";
        if (age < 18) return "Tarif enfant : 2 pièces";
        if (age < 65) return "Tarif normal : 5 pièces";
        return "Tarif aîné : 3 pièces";
    }

    static void verifier() {
        int[] cas = {-1, 0, 5, 6, 17, 18, 40, 64, 65, 120, 121};
        for (int age : cas) {
            Check.test("Âge " + age, () -> {
                var l = Check.lines(Check.runMain("Main", age + "\n"));
                Check.isTrue(!l.isEmpty(), "Rien n'est affiché.");
                Check.equal(tarif(age), l.get(l.size() - 1), "Pour l'âge " + age);
            });
        }
    }
}
