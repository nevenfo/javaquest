/** Une façon de piloter l'automate : à chaque pas, elle regarde le compagnon et choisit une direction. */
public interface Strategie {
    /**
     * Direction du prochain pas ("haut", "bas", "gauche" ou "droite"), ou {@code null} pour abandonner.
     * Elle regarde l'automate (position, capteur…) mais ne le déplace pas elle-même.
     */
    String choisir(Compagnon c);
}
