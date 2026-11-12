package fr.javaquest.sandbox;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.foreign.ValueLayout.*;

/**
 * Broker d'isolation en Java pur (FFM), équivalent de jq-sandbox.exe (launcher/Sandbox/Program.cs).
 * Exécute un programme NON FIABLE directement dans un AppContainer (optionnellement LPAC), un Job Object,
 * avec politique d'enfants interdits, délai mural, plafond de sortie, environnement minimal et liste blanche de handles.
 *   grant &lt;dossier&gt; | sid | run [--timeout-ms N --cpu-ms N --mem-mb N --max-out N --cwd D --stdin-file F --lpac --cap C] -- exe args
 */
public final class Main {
    private static final String CONTAINER_NAME = "JavaQuest.Sandbox.v1";

    public static void main(String[] args) {
        int code;
        try {
            if (args.length == 0) code = usage();
            else switch (args[0]) {
                case "sid" -> { println(sidString(containerSid())); code = 0; }
                case "grant" -> code = grant(args);
                case "run" -> code = run(args);
                default -> code = usage();
            }
        } catch (Throwable e) {
            System.err.println("jq-sandbox: " + e.getMessage());
            code = 3;
        }
        System.out.flush();
        System.err.flush();
        Runtime.getRuntime().halt(code);
    }

    private static void println(String s) { System.out.println(s); }

    private static int usage() {
        System.err.println("usage: jq-sandbox grant <dir> | sid | run [--timeout-ms N --cpu-ms N --mem-mb N --max-out N --cwd D --stdin-file F --lpac] -- exe args...");
        return 2;
    }

    private static MemorySegment wide(Arena a, String s) { return a.allocateFrom(s, StandardCharsets.UTF_16LE); }

    /** SID du conteneur (à libérer par FreeSid). */
    private static MemorySegment containerSid() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment out = a.allocate(ADDRESS);
            int hr = Win.createAppContainerProfile(wide(a, CONTAINER_NAME), wide(a, "JavaQuest Sandbox"),
                    wide(a, "Exécution isolée du code Java des joueurs JavaQuest"), out);
            if (hr == Win.HRESULT_ALREADY_EXISTS) hr = Win.deriveAppContainerSid(wide(a, CONTAINER_NAME), out);
            if (hr != Win.S_OK) throw new Exception("AppContainer indisponible (HRESULT 0x" + String.format("%08X", hr) + ")");
            return out.get(ADDRESS, 0).reinterpret(Long.MAX_VALUE);
        }
    }

    private static String sidString(MemorySegment sid) throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment out = a.allocate(ADDRESS);
            if (!Win.convertSidToStringSid(sid, out)) throw new Exception("ConvertSidToStringSid " + Win.lastError());
            MemorySegment str = out.get(ADDRESS, 0).reinterpret(Long.MAX_VALUE);
            try { return str.getString(0, StandardCharsets.UTF_16LE); } finally { Win.localFree(str); }
        }
    }

    /** Accorde RX (hérité) au SID du conteneur sur un dossier, via icacls. */
    private static int grant(String[] args) throws Throwable {
        if (args.length < 2) return usage();
        Path dirP = Path.of(args[1]).toAbsolutePath().normalize();
        String dir = dirP.toString();
        if (!Files.isDirectory(dirP)) throw new Exception("dossier introuvable : " + dir);
        String sid = sidString(containerSid());
        String sys = System.getenv("SystemRoot");
        if (sys == null) sys = "C:\\Windows";
        while (dir.endsWith("\\")) dir = dir.substring(0, dir.length() - 1);
        ProcessBuilder pb = new ProcessBuilder(Path.of(sys, "System32", "icacls.exe").toString(), dir,
                "/grant", "*" + sid + ":(OI)(CI)RX", "/Q", "/C");
        Process p = pb.start();
        p.getOutputStream().close();
        byte[] err = p.getErrorStream().readAllBytes();
        p.getInputStream().readAllBytes();
        int rc = p.waitFor();
        if (rc != 0) { System.err.println(new String(err, StandardCharsets.UTF_8)); return 4; }
        println(sid);
        return 0;
    }

    private static final class Options {
        int timeoutMs = 10000, cpuMs = 8000, memMb = 768, maxOut = 256 * 1024, cpuRate = 50;
        String cwd, stdinFile;
        boolean lpac;
        final List<String> caps = new ArrayList<>();
        final List<String> command = new ArrayList<>();
    }

    private static Options parse(String[] args) throws Exception {
        Options o = new Options();
        int i = 1;
        for (; i < args.length; i++) {
            String a = args[i];
            if (a.equals("--")) { i++; break; }
            switch (a) {
                case "--timeout-ms" -> o.timeoutMs = Integer.parseInt(args[++i]);
                case "--cpu-ms" -> o.cpuMs = Integer.parseInt(args[++i]);
                case "--mem-mb" -> o.memMb = Integer.parseInt(args[++i]);
                case "--max-out" -> o.maxOut = Integer.parseInt(args[++i]);
                case "--cpu-rate" -> o.cpuRate = Integer.parseInt(args[++i]);
                case "--cwd" -> o.cwd = args[++i];
                case "--stdin-file" -> o.stdinFile = args[++i];
                case "--lpac" -> o.lpac = true;
                case "--cap" -> o.caps.add(args[++i]);
                default -> throw new Exception("option inconnue : " + a);
            }
        }
        for (; i < args.length; i++) o.command.add(args[i]);
        if (o.command.isEmpty()) throw new Exception("commande manquante");
        return o;
    }

    private static String quote(String s) {
        if (!s.isEmpty() && s.indexOf(' ') < 0 && s.indexOf('\t') < 0 && s.indexOf('"') < 0) return s;
        StringBuilder sb = new StringBuilder("\"");
        int bs = 0;
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            if (c == '\\') { bs++; continue; }
            if (c == '"') { sb.append("\\".repeat(bs * 2 + 1)).append('"'); bs = 0; continue; }
            sb.append("\\".repeat(bs)); bs = 0; sb.append(c);
        }
        sb.append("\\".repeat(bs * 2)).append('"');
        return sb.toString();
    }

    private static void setJob(long job, int cls, MemorySegment info) throws Throwable {
        if (!Win.setInformationJobObject(job, cls, info, (int) info.byteSize()))
            throw new Exception("SetInformationJobObject(" + cls + ") " + Win.lastError());
    }

    private static long createLimitedJob(Options o, Arena a) throws Throwable {
        long job = Win.createJobObject();
        if (job == 0) throw new Exception("CreateJobObject " + Win.lastError());
        MemorySegment ext = a.allocate(Win.JOB_EXT_SIZE);
        ext.set(JAVA_INT, 16, Win.JOB_OBJECT_LIMIT_ACTIVE_PROCESS | Win.JOB_OBJECT_LIMIT_JOB_MEMORY
                | Win.JOB_OBJECT_LIMIT_PROCESS_TIME | Win.JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE
                | Win.JOB_OBJECT_LIMIT_DIE_ON_UNHANDLED_EXCEPTION);
        ext.set(JAVA_INT, 40, 1);                                          // ActiveProcessLimit
        ext.set(JAVA_LONG, 0, (long) o.cpuMs * 10000L);                    // PerProcessUserTimeLimit (100 ns)
        ext.set(JAVA_LONG, 120, (long) o.memMb * 1024L * 1024L);           // JobMemoryLimit
        setJob(job, Win.JobObjectExtendedLimitInformation, ext);
        MemorySegment ui = a.allocate(4);
        ui.set(JAVA_INT, 0, Win.JOB_OBJECT_UILIMIT_ALL);
        setJob(job, Win.JobObjectBasicUIRestrictions, ui);
        if (o.cpuRate > 0 && o.cpuRate < 100) {
            MemorySegment cr = a.allocate(8);
            cr.set(JAVA_INT, 0, Win.JOB_OBJECT_CPU_RATE_CONTROL_ENABLE | Win.JOB_OBJECT_CPU_RATE_CONTROL_HARD_CAP);
            cr.set(JAVA_INT, 4, o.cpuRate * 100);
            setJob(job, Win.JobObjectCpuRateControlInformation, cr);
        }
        return job;
    }

    private static final class Capture {
        private final long handle;
        private final int max;
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        volatile boolean overflow;
        final Thread thread;

        Capture(long handle, int max, Runnable onOverflow) {
            this.handle = handle; this.max = max;
            thread = new Thread(() -> loop(onOverflow));
            thread.setDaemon(true);
            thread.start();
        }

        private void loop(Runnable onOverflow) {
            try (Arena a = Arena.ofConfined()) {
                MemorySegment b = a.allocate(8192), nread = a.allocate(4);
                byte[] tmp = new byte[8192];
                int n;
                while ((n = Win.readFile(handle, b, 8192, nread)) > 0) {
                    int room = max - buf.size();
                    if (room > 0) {
                        int w = Math.min(room, n);
                        MemorySegment.copy(b, JAVA_BYTE, 0, tmp, 0, w);
                        synchronized (buf) { buf.write(tmp, 0, w); }
                    }
                    if (n > room && !overflow) { overflow = true; onOverflow.run(); }
                }
            } catch (Throwable ignored) { }
            try { Win.closeHandle(handle); } catch (Throwable ignored) { }
        }

        String text() { synchronized (buf) { return new String(buf.toByteArray(), StandardCharsets.UTF_8); } }
    }

    private static MemorySegment ptr(Arena a, long value) {
        MemorySegment s = a.allocate(8);
        s.set(JAVA_LONG, 0, value);
        return s;
    }

    private static int run(String[] args) throws Throwable {
        Options o = parse(args);
        String exe = Path.of(o.command.get(0)).toAbsolutePath().normalize().toString();
        StringBuilder cmd = new StringBuilder();
        for (String a : o.command) { if (cmd.length() > 0) cmd.append(' '); cmd.append(quote(a)); }

        MemorySegment sid = containerSid();
        long job = 0, hProcess = 0, hThread = 0;
        MemorySegment attrList = MemorySegment.NULL;
        boolean attrInit = false;
        try (Arena a = Arena.ofShared()) {
            MemorySegment sa = a.allocate(Win.SECURITY_ATTRIBUTES_SIZE);
            sa.set(JAVA_INT, 0, (int) Win.SECURITY_ATTRIBUTES_SIZE);
            sa.set(JAVA_INT, 16, 1);                                      // bInheritHandle
            MemorySegment p6 = a.allocate(8 * 6);
            if (!Win.createPipe(p6.asSlice(0, 8), p6.asSlice(8, 8), sa)
                    || !Win.createPipe(p6.asSlice(16, 8), p6.asSlice(24, 8), sa)
                    || !Win.createPipe(p6.asSlice(32, 8), p6.asSlice(40, 8), sa))
                throw new Exception("CreatePipe " + Win.lastError());
            long inR = p6.get(JAVA_LONG, 0), inW = p6.get(JAVA_LONG, 8);
            long outR = p6.get(JAVA_LONG, 16), outW = p6.get(JAVA_LONG, 24);
            long errR = p6.get(JAVA_LONG, 32), errW = p6.get(JAVA_LONG, 40);
            Win.setHandleInformation(inW, Win.HANDLE_FLAG_INHERIT, 0);
            Win.setHandleInformation(outR, Win.HANDLE_FLAG_INHERIT, 0);
            Win.setHandleInformation(errR, Win.HANDLE_FLAG_INHERIT, 0);

            // Liste d'attributs : capacités (AppContainer), liste blanche de handles, interdiction d'enfants, LPAC.
            int attrCount = o.lpac ? 4 : 3;
            MemorySegment size = a.allocate(8);
            Win.initAttr(MemorySegment.NULL, attrCount, size);
            attrList = a.allocate(size.get(JAVA_LONG, 0));
            if (!Win.initAttr(attrList, attrCount, size)) throw new Exception("InitializeProcThreadAttributeList " + Win.lastError());
            attrInit = true;
            try {
                MemorySegment capArray = MemorySegment.NULL;
                if (!o.caps.isEmpty()) {
                    capArray = a.allocate(Win.SID_AND_ATTRIBUTES_SIZE * o.caps.size());
                    for (int c = 0; c < o.caps.size(); c++) {
                        MemorySegment g = a.allocate(8), gc = a.allocate(4), cs = a.allocate(8), cc = a.allocate(4);
                        if (!Win.deriveCapabilitySidsFromName(wide(a, o.caps.get(c)), g, gc, cs, cc) || cc.get(JAVA_INT, 0) < 1)
                            throw new Exception("capacité inconnue : " + o.caps.get(c));
                        capArray.set(ADDRESS, c * Win.SID_AND_ATTRIBUTES_SIZE, cs.get(ADDRESS, 0).reinterpret(Long.MAX_VALUE).get(ADDRESS, 0));
                        capArray.set(JAVA_INT, c * Win.SID_AND_ATTRIBUTES_SIZE + 8, Win.SE_GROUP_ENABLED);
                    }
                }
                MemorySegment caps = a.allocate(Win.SECURITY_CAPABILITIES_SIZE);
                caps.set(ADDRESS, 0, sid);
                caps.set(ADDRESS, 8, capArray);
                caps.set(JAVA_INT, 16, o.caps.size());
                if (!Win.updateAttr(attrList, Win.PROC_THREAD_ATTRIBUTE_SECURITY_CAPABILITIES, caps, Win.SECURITY_CAPABILITIES_SIZE))
                    throw new Exception("attribut SECURITY_CAPABILITIES " + Win.lastError());

                MemorySegment handles = a.allocate(8 * 3);
                handles.set(JAVA_LONG, 0, inR);
                handles.set(JAVA_LONG, 8, outW);
                handles.set(JAVA_LONG, 16, errW);
                if (!Win.updateAttr(attrList, Win.PROC_THREAD_ATTRIBUTE_HANDLE_LIST, handles, 8 * 3))
                    throw new Exception("attribut HANDLE_LIST " + Win.lastError());

                MemorySegment childPolicy = a.allocate(4);
                childPolicy.set(JAVA_INT, 0, Win.PROCESS_CREATION_CHILD_PROCESS_RESTRICTED);
                if (!Win.updateAttr(attrList, Win.PROC_THREAD_ATTRIBUTE_CHILD_PROCESS_POLICY, childPolicy, 4))
                    throw new Exception("attribut CHILD_PROCESS_POLICY " + Win.lastError());

                if (o.lpac) {
                    MemorySegment lpac = a.allocate(4);
                    lpac.set(JAVA_INT, 0, Win.PROCESS_CREATION_ALL_APPLICATION_PACKAGES_OPT_OUT);
                    if (!Win.updateAttr(attrList, Win.PROC_THREAD_ATTRIBUTE_ALL_APPLICATION_PACKAGES_POLICY, lpac, 4))
                        throw new Exception("attribut LPAC " + Win.lastError());
                }

                // Environnement minimal (identique à jq-sandbox.exe).
                String sysRoot = System.getenv("SystemRoot");
                if (sysRoot == null) sysRoot = "C:\\Windows";
                StringBuilder envSb = new StringBuilder();
                for (String k : new String[]{"SystemRoot", "SystemDrive", "windir", "USERPROFILE", "LOCALAPPDATA", "APPDATA",
                        "TEMP", "TMP", "NUMBER_OF_PROCESSORS", "PROCESSOR_ARCHITECTURE"}) {
                    String v = System.getenv(k);
                    if (v != null && !v.isEmpty()) envSb.append(k).append('=').append(v).append('\0');
                }
                envSb.append("PATH=").append(Path.of(sysRoot, "System32")).append('\0').append('\0');
                byte[] envBytes = envSb.toString().getBytes(StandardCharsets.UTF_16LE);
                MemorySegment pEnv = a.allocate(envBytes.length + 4);
                MemorySegment.copy(envBytes, 0, pEnv, JAVA_BYTE, 0, envBytes.length);

                MemorySegment si = a.allocate(Win.STARTUPINFOEX_SIZE);
                si.set(JAVA_INT, 0, (int) Win.STARTUPINFOEX_SIZE);
                si.set(JAVA_INT, 60, Win.STARTF_USESTDHANDLES);
                si.set(JAVA_LONG, 80, inR);
                si.set(JAVA_LONG, 88, outW);
                si.set(JAVA_LONG, 96, errW);
                si.set(ADDRESS, 104, attrList);

                job = createLimitedJob(o, a);
                String cwd = o.cwd != null ? o.cwd : Path.of(exe).getParent().toString();
                MemorySegment pi = a.allocate(Win.PROCESS_INFORMATION_SIZE);
                if (!Win.createProcess(wide(a, exe), wide(a, cmd.toString()), true,
                        Win.EXTENDED_STARTUPINFO_PRESENT | Win.CREATE_SUSPENDED | Win.DETACHED_PROCESS | Win.CREATE_UNICODE_ENVIRONMENT,
                        pEnv, wide(a, cwd), si, pi))
                    throw new Exception("CreateProcess " + Win.lastError());
                hProcess = pi.get(JAVA_LONG, 0);
                hThread = pi.get(JAVA_LONG, 8);
                if (!Win.assignProcessToJobObject(job, hProcess)) {
                    int err = Win.lastError();
                    Win.terminateJobObject(job, 1);
                    throw new Exception("AssignProcessToJobObject " + err);
                }
                Win.closeHandle(inR); Win.closeHandle(outW); Win.closeHandle(errW);

                final long jobForKill = job;
                Runnable kill = () -> { try { Win.terminateJobObject(jobForKill, 0xC0000409); } catch (Throwable ignored) { } };
                Capture outCap = new Capture(outR, o.maxOut, kill);
                Capture errCap = new Capture(errR, o.maxOut, kill);

                long t0 = System.nanoTime();
                Win.resumeThread(hThread);

                Thread feeder = new Thread(() -> feed(o, inW));
                feeder.setDaemon(true);
                feeder.start();

                boolean timedOut = Win.waitForSingleObject(hProcess, o.timeoutMs) == Win.WAIT_TIMEOUT;
                if (timedOut) { Win.terminateJobObject(job, 0xC0000420); Win.waitForSingleObject(hProcess, 3000); }
                long wallMs = (System.nanoTime() - t0) / 1_000_000L;
                MemorySegment exitSeg = a.allocate(4);
                int exit = Win.getExitCodeProcess(hProcess, exitSeg);
                outCap.thread.join(2000); errCap.thread.join(2000);

                MemorySegment ext = a.allocate(Win.JOB_EXT_SIZE);
                Win.queryInformationJobObject(job, Win.JobObjectExtendedLimitInformation, ext, (int) Win.JOB_EXT_SIZE);
                MemorySegment acct = a.allocate(Win.JOB_ACCT_SIZE);
                Win.queryInformationJobObject(job, Win.JobObjectBasicAccountingInformation, acct, (int) Win.JOB_ACCT_SIZE);

                long peak = ext.get(JAVA_LONG, 136);
                long cpuMs = acct.get(JAVA_LONG, 0) / 10000;
                boolean memLimit = Long.compareUnsigned(peak, (long) o.memMb * 1024L * 1024L * 95L / 100L) >= 0;
                boolean cpuLimit = cpuMs >= o.cpuMs || exit == 0xC0000044 /*STATUS_QUOTA_EXCEEDED*/ || exit == 0x705 /*ERROR_NOT_ENOUGH_QUOTA*/;

                StringBuilder json = new StringBuilder("{");
                json.append("\"exitCode\":").append(exit);
                json.append(",\"timedOut\":").append(timedOut);
                json.append(",\"outputLimit\":").append(outCap.overflow || errCap.overflow);
                json.append(",\"memoryLimit\":").append(memLimit);
                json.append(",\"cpuLimit\":").append(cpuLimit);
                json.append(",\"wallMs\":").append(wallMs);
                json.append(",\"cpuMs\":").append(cpuMs);
                json.append(",\"peakMemMb\":").append(peak / (1024 * 1024));
                json.append(",\"lpac\":").append(o.lpac);
                json.append(",\"stdout\":").append(jsonStr(outCap.text()));
                json.append(",\"stderr\":").append(jsonStr(errCap.text()));
                json.append("}");
                System.out.write(json.toString().getBytes(StandardCharsets.UTF_8));
                System.out.flush();
                return 0;
            } finally {
                if (hThread != 0) Win.closeHandle(hThread);
                if (hProcess != 0) Win.closeHandle(hProcess);
                if (job != 0) { Win.terminateJobObject(job, 1); Win.closeHandle(job); }
                if (attrInit) Win.deleteAttr(attrList);
                Win.freeSid(sid);
            }
        }
    }

    /** Copie stdin (ou --stdin-file) vers le tube d'entrée de l'enfant, puis ferme ce tube. */
    private static void feed(Options o, long inW) {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment b = a.allocate(8192), nw = a.allocate(4);
            byte[] tmp = new byte[8192];
            try (InputStream in = o.stdinFile != null ? Files.newInputStream(Path.of(o.stdinFile)) : System.in) {
                int n;
                while ((n = in.read(tmp)) > 0) {
                    MemorySegment.copy(tmp, 0, b, JAVA_BYTE, 0, n);
                    if (!Win.writeFile(inW, b, n, nw)) break;
                }
            }
        } catch (Throwable ignored) { }
        try { Win.closeHandle(inW); } catch (Throwable ignored) { }
    }

    private static String jsonStr(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 0x20) sb.append("\\u").append(String.format("%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.append('"').toString();
    }
}
