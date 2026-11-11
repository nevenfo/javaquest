package fr.javaquest.server;

import fr.javaquest.common.Json;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Sauvegardes locales par profil : {@code <data>/profiles/<id>.json}, écriture atomique
 * (fichier temporaire + fsync + remplacement) et copie précédente {@code .bak}.
 */
public final class Storage {
    private static final Pattern ID = Pattern.compile("[a-z0-9-]{1,40}");
    public static final long MAX_SAVE_BYTES = 4L * 1024 * 1024;
    private final Path dir;

    public Storage(Path dataDir) throws IOException {
        this.dir = dataDir.resolve("profiles");
        Files.createDirectories(dir);
    }

    private Path file(String id) {
        if (id == null || !ID.matcher(id).matches()) throw new IllegalArgumentException("identifiant de profil invalide");
        return dir.resolve(id + ".json");
    }

    public synchronized List<Map<String, Object>> list() throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : ds) {
                String id = p.getFileName().toString().replaceFirst("\\.json$", "");
                if (!ID.matcher(id).matches()) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", id);
                try {
                    Map<String, Object> save = Json.parseObject(Files.readString(p));
                    m.put("name", Json.str(Json.obj(save, "player"), "name", id));
                    m.put("updated", Json.str(save, "updated", ""));
                } catch (RuntimeException | IOException e) {
                    m.put("name", id);
                    m.put("corrupted", true);
                }
                out.add(m);
            }
        }
        out.sort(Comparator.comparing(m -> String.valueOf(m.get("updated")), Comparator.reverseOrder()));
        return out;
    }

    /** Crée un identifiant libre dérivé du nom. */
    public synchronized String newId(String name) {
        String base = java.text.Normalizer.normalize(name == null ? "" : name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isEmpty()) base = "joueur";
        if (base.length() > 30) base = base.substring(0, 30);
        String id = base;
        for (int k = 2; Files.exists(dir.resolve(id + ".json")); k++) id = base + "-" + k;
        return id;
    }

    /** Lit une sauvegarde ; si le fichier principal est illisible, tente la copie .bak. */
    public synchronized String load(String id) throws IOException {
        Path p = file(id);
        for (Path c : List.of(p, p.resolveSibling(id + ".json.bak"))) {
            if (!Files.exists(c)) continue;
            String s = Files.readString(c, StandardCharsets.UTF_8);
            try { Json.parseObject(s); return s; } catch (RuntimeException ignored) { }
        }
        return null;
    }

    public synchronized void save(String id, String json) throws IOException {
        if (json.length() > MAX_SAVE_BYTES) throw new IllegalArgumentException("sauvegarde trop volumineuse");
        Map<String, Object> m = Json.parseObject(json);
        if (!(m.get("schema") instanceof Number)) throw new IllegalArgumentException("champ schema manquant");
        m.put("updated", Instant.now().toString());
        Path p = file(id);
        Path tmp = p.resolveSibling(id + ".json.tmp");
        Files.writeString(tmp, Json.write(m), StandardCharsets.UTF_8);
        try (FileChannel ch = FileChannel.open(tmp, StandardOpenOption.WRITE)) { ch.force(true); }
        if (Files.exists(p)) Files.copy(p, p.resolveSibling(id + ".json.bak"), StandardCopyOption.REPLACE_EXISTING);
        try {
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Suppression réversible : le profil est renommé en .deleted-<date>. */
    public synchronized void delete(String id) throws IOException {
        Path p = file(id);
        long now = System.currentTimeMillis();
        if (Files.exists(p)) Files.move(p, p.resolveSibling(id + ".json.deleted-" + now));
        Path bak = p.resolveSibling(id + ".json.bak");
        if (Files.exists(bak)) Files.move(bak, p.resolveSibling(id + ".json.bak.deleted-" + now));
    }
}
