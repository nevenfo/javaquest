/** Une commande a été mal utilisée (argument manquant ou invalide). Le message explique pourquoi. */
public class CommandeException extends Exception {
    public CommandeException(String message) { super(message); }
}
