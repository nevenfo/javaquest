import fr.javaquest.runner.Check;

class Verif {
    static void essai(String... mots) {
        Check.test("Inscriptions " + String.join(", ", mots), () -> {
            int ombres = 0, e = 0;
            StringBuilder in = new StringBuilder();
            for (String m : mots) {
                in.append(m).append("\n");
                if (m.equals("ombre")) ombres++;
                for (char c : m.toCharArray()) if (c == 'e') e++;
            }
            in.append("fin\n");
            var l = Check.lines(Check.runMain("Main", in.toString()));
            Check.isTrue(l.size() >= 2, "Il faut afficher deux lignes (Ombres, Lettres e). Obtenu : " + l);
            Check.equal("Ombres : " + ombres, l.get(l.size() - 2), "Ligne Ombres");
            Check.equal("Lettres e : " + e, l.get(l.size() - 1), "Ligne Lettres e");
        });
    }

    static void verifier() {
        essai();
        essai("ombre", "lueur", "ombre");
        essai("eau", "e", "ee");
        essai("Ombre", "ombrelle", "ombre");
        essai("Eole", "tige", "ombre", "ombre", "ombre");
        essai("ciel");
    }
}
