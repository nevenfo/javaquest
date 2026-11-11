package fr.javaquest.server;

import fr.javaquest.common.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Contenu pédagogique : {@code content/quests/<id>/quest.json} + fichiers Java.
 * Partie publique (énoncé, code de départ, API de quête) envoyée au jeu ; harnais de
 * vérification, solutions de référence et solutions fautives restent côté serveur.
 */
public final class Content {
    public record Quest(String id, Map<String, Object> meta, List<Map<String, Object>> support,
                        List<Map<String, Object>> harness, List<List<Map<String, Object>>> solutions,
                        List<List<Map<String, Object>>> wrong) { }

    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private final Map<String, Object> skills;

    public Content(Path dir) throws IOException {
        Path skillsFile = dir.resolve("skills.json");
        skills = Files.exists(skillsFile) ? Json.parseObject(Files.readString(skillsFile, StandardCharsets.UTF_8)) : Map.of();
        Path qdir = dir.resolve("quests");
        if (!Files.isDirectory(qdir)) return;
        List<Path> dirs;
        try (Stream<Path> s = Files.list(qdir)) { dirs = s.filter(Files::isDirectory).sorted().toList(); }
        for (Path q : dirs) {
            Path f = q.resolve("quest.json");
            if (!Files.exists(f)) continue;
            Map<String, Object> meta = Json.parseObject(Files.readString(f, StandardCharsets.UTF_8));
            String id = Json.str(meta, "id", q.getFileName().toString());
            quests.put(id, new Quest(id, meta, files(q, Json.list(meta, "support")), files(q, Json.list(meta, "harness")),
                    fileSets(q, Json.list(meta, "solutions")), fileSets(q, Json.list(meta, "wrong"))));
        }
    }

    private static List<Map<String, Object>> files(Path base, List<Object> paths) throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : paths) {
            Path p = base.resolve(String.valueOf(o)).normalize();
            if (!p.startsWith(base)) throw new IOException("chemin hors quête : " + o);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", p.getFileName().toString());
            m.put("content", Files.readString(p, StandardCharsets.UTF_8));
            out.add(m);
        }
        return out;
    }

    private static List<List<Map<String, Object>>> fileSets(Path base, List<Object> sets) throws IOException {
        List<List<Map<String, Object>>> out = new ArrayList<>();
        for (Object s : sets) out.add(files(base, s instanceof List<?> l ? new ArrayList<>(l) : List.of(s)));
        return out;
    }

    public Quest quest(String id) { return quests.get(id); }

    public Collection<Quest> all() { return quests.values(); }

    /** Vue publique (sans harnais, solutions ni solutions fautives). */
    public Map<String, Object> publicView() {
        List<Object> list = new ArrayList<>();
        for (Quest q : quests.values()) {
            Map<String, Object> m = new LinkedHashMap<>(q.meta());
            m.remove("harness"); m.remove("solutions"); m.remove("wrong");
            m.put("support", q.support());
            list.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("skills", skills);
        out.put("quests", list);
        return out;
    }
}
