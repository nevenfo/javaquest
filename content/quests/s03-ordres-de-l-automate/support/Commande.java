import java.util.List;

/** Un ordre que l'automate sait exécuter. */
public interface Commande {
    /** Exécute l'ordre avec ses arguments (les mots après le nom). Lève {@link CommandeException} si les arguments sont invalides. */
    void executer(Compagnon c, List<String> args) throws CommandeException;
}
