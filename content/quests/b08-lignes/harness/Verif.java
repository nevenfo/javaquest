
import fr.javaquest.runner.Check;
import java.util.Arrays;

class Verif {
    static void verifier() {
        Check.test("position : ligne simple", () -> {
            Check.equal("[12, 7]", Arrays.toString(Main.position("12,7:coffre")), "position(\"12,7:coffre\")");
            Check.equal("[0, 0]", Arrays.toString(Main.position("0,0:x")), "position(\"0,0:x\")");
        });
        Check.test("position : espaces et négatifs", () -> {
            Check.equal("[3, 40]", Arrays.toString(Main.position(" 3 , 40 : épée magique ")), "position avec espaces");
            Check.equal("[-5, 9]", Arrays.toString(Main.position("-5,9:clé")), "position avec un négatif");
        });
        Check.test("objet", () -> {
            Check.equal("coffre", Main.objet("12,7:coffre"), "objet(\"12,7:coffre\")");
            Check.equal("clé", Main.objet("-5,9:clé"), "objet(\"-5,9:clé\")");
        });
        Check.test("objet : espaces retirés", () -> {
            Check.equal("épée magique", Main.objet(" 3 , 40 : épée magique "), "objet avec espaces autour");
        });
    }
}
