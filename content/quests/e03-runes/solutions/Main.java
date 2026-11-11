import java.util.*;

boolean palindrome(String s) {
    for (int i = 0; i < s.length() / 2; i++)
        if (s.charAt(i) != s.charAt(s.length() - 1 - i)) return false;
    return true;
}

boolean uneSeuleLettre(String s) {
    for (int i = 1; i < s.length(); i++) if (s.charAt(i) != s.charAt(0)) return false;
    return true;
}

void main() {
    Set<String> vues = new HashSet<>();
    String doublon = null, meilleur = null;
    String rune = IO.readln();
    while (rune != null && !rune.equals("FIN")) {
        if (!vues.add(rune) && doublon == null) doublon = rune;
        if (palindrome(rune) && !uneSeuleLettre(rune) && (meilleur == null || rune.length() > meilleur.length())) meilleur = rune;
        rune = IO.readln();
    }
    IO.println("Doublon : " + doublon);
    IO.println("Plus long palindrome : " + (meilleur == null ? "aucun" : meilleur));
}
