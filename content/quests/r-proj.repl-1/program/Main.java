import java.util.*;

interface Commande { String executer(List<String> args); }

void main() {
    Map<String, Commande> cmds = new TreeMap<>();
    List<String> notes = new ArrayList<>();
    cmds.put("note", args -> { notes.addAll(args); return null; });
    cmds.put("nb", args -> String.valueOf(notes.size()));
    cmds.put("dernier", args -> notes.isEmpty() ? "aucune" : notes.get(notes.size() - 1));
    String[] script = { "nb", "note a b", "nb", "dernier", "saut", "note   c", "dernier", "fin", "nb" };
    for (String ligne : script) {
        if (ligne.equals("fin")) break;
        String[] mots = ligne.trim().split("\s+");
        Commande c = cmds.get(mots[0]);
        String r = c == null ? "? " + mots[0] : c.executer(Arrays.asList(mots).subList(1, mots.length));
        if (r != null) IO.println(r);
    }
    IO.println(cmds.keySet());
}
