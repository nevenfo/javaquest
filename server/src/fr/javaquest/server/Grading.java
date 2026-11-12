package fr.javaquest.server;

import fr.javaquest.common.Json;

import java.util.*;

/** Notation des formats sans code à écrire : questions à choix (clé cachée) et prédiction de sortie (exécution réelle). */
public final class Grading {
    private Grading() { }

    /** answers : une liste d'indices cochés par question ; answerKey (jamais envoyée au client) idem. */
    public static Map<String, Object> quiz(Content.Quest q, List<Object> answers) {
        List<Object> key = Json.list(q.meta(), "answerKey");
        List<Object> questions = Json.list(q.meta(), "questions");
        List<Object> explanations = Json.list(q.meta(), "explanations");
        List<Object> tests = new ArrayList<>();
        boolean all = !key.isEmpty();
        for (int i = 0; i < key.size(); i++) {
            Set<Long> expected = indices(key.get(i)), given = i < answers.size() ? indices(answers.get(i)) : Set.of();
            boolean ok = expected.equals(given);
            all &= ok;
            Map<String, Object> t = new LinkedHashMap<>();
            String title = i < questions.size() && questions.get(i) instanceof Map<?, ?> m ? String.valueOf(m.get("q")) : "Question " + (i + 1);
            t.put("name", "Q" + (i + 1) + ". " + (title.length() > 90 ? title.substring(0, 90) + "…" : title));
            t.put("ok", ok);
            // Rétroaction informative sans révéler la clé : explication seulement après réponse correcte.
            if (ok && i < explanations.size()) t.put("message", String.valueOf(explanations.get(i)));
            if (!ok) t.put("message", given.isEmpty() ? "Aucune réponse cochée." : "Réponse incorrecte ou incomplète — relis chaque proposition.");
            tests.add(t);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("status", "ok");
        r.put("tests", tests);
        r.put("passed", all);
        return r;
    }

    private static Set<Long> indices(Object o) {
        Set<Long> s = new TreeSet<>();
        if (o instanceof List<?> l) for (Object x : l) if (x instanceof Number n) s.add(n.longValue());
        if (o instanceof Number n) s.add(n.longValue());
        return s;
    }

    /** Exécute réellement le programme de la quête et compare sa sortie à la prédiction (lignes, espaces de fin ignorés). */
    public static Map<String, Object> predict(Content.Quest q, String prediction, SandboxRunner runner) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("mode", "run");
        req.put("files", q.program());
        req.put("support", q.support());
        req.put("stdin", Json.str(q.meta(), "stdin", ""));
        Map<String, Object> r = runner.run(req, SandboxRunner.Limits.RUN);
        String actual = String.valueOf(r.getOrDefault("stdout", ""));
        boolean ok = "ok".equals(r.get("status")) && norm(actual).equals(norm(prediction));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("name", "Ta prédiction correspond à la sortie réelle");
        t.put("ok", ok);
        if (!ok) t.put("message", "Sortie réelle :\n" + actual + "\nCompare ligne par ligne, puis trace le programme à la main pour comprendre l'écart.");
        r.put("tests", List.of(t));
        r.put("passed", ok);
        r.put("status", "ok");
        return r;
    }

    static List<String> norm(String s) {
        List<String> out = new ArrayList<>();
        for (String l : s.replace("\r\n", "\n").split("\n")) out.add(l.stripTrailing());
        while (!out.isEmpty() && out.get(out.size() - 1).isEmpty()) out.remove(out.size() - 1);
        return out;
    }
}
