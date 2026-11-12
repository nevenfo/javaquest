package fr.javaquest.server;

import fr.javaquest.common.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Exécute le runner Java DANS jq-sandbox (AppContainer LPAC + Job Object) et décode le protocole.
 * Aucun repli non isolé : si la sandbox échoue, le résultat est une erreur explicite.
 */
public final class SandboxRunner {
    private final Path sandboxExe, javaExe, runnerJar;
    private final Semaphore slots = new Semaphore(2);
    private final SecureRandom random = new SecureRandom();
    private volatile String grantError;

    public SandboxRunner(Path sandboxExe, Path runtimeDir, Path runnerJar) {
        this.sandboxExe = sandboxExe;
        this.javaExe = runtimeDir.resolve("bin").resolve("java.exe");
        this.runnerJar = runnerJar;
    }

    /** Accorde au conteneur la lecture du runtime et du runner (idempotent, sans droits admin). */
    public void prepare(Path runtimeDir, Path stateDir) {
        try {
            Path marker = stateDir.resolve("sandbox-grant.txt");
            String key = runtimeDir.toAbsolutePath() + "|" + runnerJar.getParent().toAbsolutePath();
            if (Files.exists(marker) && Files.readString(marker).equals(key)) return;
            for (Path p : List.of(runtimeDir, runnerJar.getParent())) {
                Process pr = new ProcessBuilder(sandboxExe.toString(), "grant", p.toAbsolutePath().toString())
                        .redirectErrorStream(true).start();
                String out = new String(pr.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (!pr.waitFor(60, TimeUnit.SECONDS) || pr.exitValue() != 0)
                    throw new IOException("autorisation refusée pour " + p + " : " + out.strip());
            }
            Files.createDirectories(stateDir);
            Files.writeString(marker, key);
            grantError = null;
        } catch (Exception e) {
            grantError = "La sandbox n'a pas pu être préparée (" + e.getMessage() + "). L'exécution de code est désactivée.";
        }
    }

    public String grantError() { return grantError; }

    public record Limits(int timeoutMs, int cpuMs, int memMb) {
        public static final Limits RUN = new Limits(10_000, 8_000, 768);
        public static final Limits TEST = new Limits(20_000, 16_000, 768);
    }

    /**
     * @param request requête runner (sans nonce, ajouté ici)
     * @return résultat structuré : compile[], output[], events[], watches[], exception, tests[], limits, status
     */
    public Map<String, Object> run(Map<String, Object> request, Limits limits) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (grantError != null) { result.put("status", "sandbox-unavailable"); result.put("error", grantError); return result; }
        byte[] n = new byte[18];
        random.nextBytes(n);
        String nonce = HexFormat.of().formatHex(n);
        Map<String, Object> req = new LinkedHashMap<>(request);
        req.put("nonce", nonce);
        boolean acquired = false;
        try {
            acquired = slots.tryAcquire(30, TimeUnit.SECONDS);
            if (!acquired) { result.put("status", "busy"); result.put("error", "Trop d'exécutions simultanées."); return result; }
            List<String> cmd = List.of(sandboxExe.toString(), "run", "--lpac", "--cap", "registryRead",
                    "--timeout-ms", String.valueOf(limits.timeoutMs()), "--cpu-ms", String.valueOf(limits.cpuMs()),
                    "--mem-mb", String.valueOf(limits.memMb()), "--max-out", String.valueOf(2 * 1024 * 1024), "--",
                    javaExe.toString(), "-XX:-UsePerfData", "-Xms8m", "-Xmx256m", "-Xss1m", "-XX:+UseSerialGC",
                    "-XX:TieredStopAtLevel=1", "-XX:CICompilerCount=1", "-Dline.separator=\n", "-Dfile.encoding=UTF-8",
                    "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-Dstdin.encoding=UTF-8", "-Duser.language=fr", "-Duser.country=FR",
                    "-cp", runnerJar.toAbsolutePath().toString(), "fr.javaquest.runner.Runner");
            Process p = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try (OutputStream os = p.getOutputStream()) { os.write(Json.write(req).getBytes(StandardCharsets.UTF_8)); }
            String brokerOut = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!p.waitFor(limits.timeoutMs() + 15_000L, TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                result.put("status", "error"); result.put("error", "La sandbox ne répond pas.");
                return result;
            }
            if (p.exitValue() != 0 || brokerOut.isBlank()) {
                result.put("status", "sandbox-unavailable");
                result.put("error", "Échec de la sandbox (code " + p.exitValue() + "). L'exécution non isolée est refusée.");
                return result;
            }
            Map<String, Object> broker = Json.parseObject(brokerOut);
            decode(nonce, Json.str(broker, "stdout", ""), result);
            Map<String, Object> lim = new LinkedHashMap<>();
            for (String k : List.of("timedOut", "cpuLimit", "memoryLimit", "outputLimit")) lim.put(k, Json.bool(broker, k, false));
            lim.put("wallMs", Json.num(broker, "wallMs", 0));
            lim.put("peakMemMb", Json.num(broker, "peakMemMb", 0));
            result.put("limits", lim);
            if (!result.containsKey("status")) {
                if (Json.bool(broker, "timedOut", false) || Json.bool(broker, "cpuLimit", false)) result.put("status", "timeout");
                else if (Json.bool(broker, "memoryLimit", false)) result.put("status", "memory");
                else if (Json.bool(broker, "outputLimit", false)) result.put("status", "output-limit");
                else {
                    result.put("status", "crash");
                    String err = Json.str(broker, "stderr", "");
                    result.put("error", "La JVM s'est arrêtée brutalement (code " + Json.num(broker, "exitCode", -1) + ")"
                            + (err.isBlank() ? "" : " : " + err.substring(0, Math.min(400, err.length()))));
                }
            }
            return result;
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", "Erreur interne d'exécution : " + e.getMessage());
            return result;
        } finally {
            if (acquired) slots.release();
        }
    }

    /** Décode les lignes \u0001nonce TYPE json ; ignore tout le reste (sortie forgée). */
    static void decode(String nonce, String stdout, Map<String, Object> result) {
        String prefix = "\u0001" + nonce + " ";
        List<Object> compile = new ArrayList<>(), timeline = new ArrayList<>(), tests = new ArrayList<>();
        StringBuilder out = new StringBuilder(), err = new StringBuilder();
        for (String line : stdout.split("\n")) {
            if (!line.startsWith(prefix) || line.length() < prefix.length() + 2) continue;
            char type = line.charAt(prefix.length());
            Map<String, Object> m;
            try { m = Json.parseObject(line.substring(prefix.length() + 2)); } catch (RuntimeException e) { continue; }
            switch (type) {
                case 'C' -> compile.add(m);
                case 'O' -> { m.put("t", "out"); timeline.add(m); ("err".equals(Json.str(m, "s", "out")) ? err : out).append(Json.str(m, "text", "")); }
                case 'E' -> { m.put("t", "event"); timeline.add(m); }
                case 'W' -> { m.put("t", "watch"); timeline.add(m); }
                case 'X' -> { result.put("exception", m); m.put("t", "exception"); timeline.add(m); }
                case 'T' -> tests.add(m);
                case 'L' -> result.put("truncated", Json.str(m, "reason", "?"));
                case 'D' -> result.put("status", Json.str(m, "status", "ok"));
                default -> { }
            }
        }
        result.put("compile", compile);
        result.put("timeline", timeline);
        result.put("stdout", out.toString());
        result.put("stderr", err.toString());
        result.put("tests", tests);
    }
}
