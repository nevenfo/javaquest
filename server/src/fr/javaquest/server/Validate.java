package fr.javaquest.server;

import fr.javaquest.common.Json;

import java.nio.file.Path;
import java.util.*;

/**
 * Validation du contenu pédagogique : pour chaque quête, chaque solution de référence doit
 * compiler et réussir tous les tests du harnais ; chaque solution fautive connue doit échouer ;
 * le code de départ doit compiler (ou être explicitement marqué incomplet) et ne pas réussir.
 * Usage : java -cp server.jar fr.javaquest.server.Validate --content D --runtime D --sandbox exe --runner jar --data D [--only id]
 */
public final class Validate {
    public static void main(String[] args) throws Exception {
        Map<String, String> o = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) o.put(args[i].replaceFirst("^--", ""), args[i + 1]);
        Path runtime = Path.of(o.get("runtime"));
        SandboxRunner runner = new SandboxRunner(Path.of(o.get("sandbox")), runtime, Path.of(o.get("runner")));
        runner.prepare(runtime, Path.of(o.get("data")));
        if (runner.grantError() != null) { System.out.println("ÉCHEC sandbox : " + runner.grantError()); System.exit(2); }
        Content content = new Content(Path.of(o.get("content")));
        Set<String> skills = new HashSet<>();
        Object sk = content.publicView().get("skills");
        if (sk instanceof Map<?, ?> sm && sm.get("skills") instanceof List<?> l)
            for (Object s : l) if (s instanceof Map<?, ?> m) skills.add(String.valueOf(m.get("id")));
        int ok = 0, ko = 0;
        String only = o.get("only");
        for (Content.Quest q : content.all()) {
            if (only != null && !q.id().equals(only)) continue;
            List<String> problems = new ArrayList<>();
            for (Object s : Json.list(q.meta(), "skills"))
                if (!skills.isEmpty() && !skills.contains(String.valueOf(s))) problems.add("compétence inconnue : " + s);
            if (q.harness().isEmpty() && !"lore".equals(Json.str(q.meta(), "kind", ""))) problems.add("aucun harnais");
            if (q.solutions().isEmpty() && !q.harness().isEmpty()) problems.add("aucune solution de référence");
            for (List<Map<String, Object>> sol : q.solutions()) {
                Map<String, Object> r = check(runner, q, sol);
                if (!Boolean.TRUE.equals(r.get("passed"))) problems.add("solution " + names(sol) + " échoue : " + brief(r));
            }
            for (List<Map<String, Object>> w : q.wrong()) {
                Map<String, Object> r = check(runner, q, w);
                if (Boolean.TRUE.equals(r.get("passed"))) problems.add("solution fautive " + names(w) + " acceptée à tort");
            }
            Map<String, Object> starter = Json.obj(q.meta(), "starter");
            if (!starter.isEmpty() && !q.harness().isEmpty()) {
                List<Map<String, Object>> files = new ArrayList<>();
                starter.forEach((k, v) -> files.add(Map.of("name", k, "content", String.valueOf(v))));
                Map<String, Object> r = check(runner, q, files);
                if (Boolean.TRUE.equals(r.get("passed"))) problems.add("le code de départ réussit déjà la quête");
            }
            if (problems.isEmpty()) { ok++; System.out.println("OK    " + q.id()); }
            else { ko++; System.out.println("ÉCHEC " + q.id()); problems.forEach(p -> System.out.println("      - " + p)); }
        }
        System.out.println("RÉSULTAT : " + ok + " quêtes valides, " + ko + " en échec");
        System.exit(ko == 0 ? 0 : 1);
    }

    private static Map<String, Object> check(SandboxRunner runner, Content.Quest q, List<Map<String, Object>> files) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("mode", "test");
        req.put("files", files);
        req.put("support", q.support());
        req.put("harness", q.harness());
        req.put("entry", Json.str(q.meta(), "entry", "Verif"));
        Map<String, Object> r = runner.run(req, SandboxRunner.Limits.TEST);
        List<?> tests = r.get("tests") instanceof List<?> l ? l : List.of();
        boolean all = !tests.isEmpty() && tests.stream().allMatch(t -> t instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("ok")));
        r.put("passed", all && "ok".equals(r.get("status")));
        return r;
    }

    private static String names(List<Map<String, Object>> files) {
        return files.stream().map(f -> String.valueOf(f.get("name"))).toList().toString();
    }

    private static String brief(Map<String, Object> r) {
        StringBuilder b = new StringBuilder(String.valueOf(r.get("status")));
        if (r.get("compile") instanceof List<?> cl) for (Object c : cl)
            if (c instanceof Map<?, ?> m && Boolean.FALSE.equals(m.get("ok"))) b.append(" | compilation ").append(m.get("phase")).append(" : ").append(m.get("diagnostics"));
        if (r.get("tests") instanceof List<?> tl) for (Object t : tl)
            if (t instanceof Map<?, ?> m && !Boolean.TRUE.equals(m.get("ok"))) b.append(" | ").append(m.get("name")).append(" : ").append(m.get("message"));
        if (r.get("error") != null) b.append(" | ").append(r.get("error"));
        String s = b.toString();
        return s.length() > 700 ? s.substring(0, 700) + "…" : s;
    }
}
