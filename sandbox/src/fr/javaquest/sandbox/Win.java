package fr.javaquest.sandbox;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

/** Déclarations Win32 minimales (FFM) : AppContainer, Job Objects, processus, tubes. Équivalent de Native.cs. */
final class Win {
    private Win() {}

    static final int S_OK = 0;
    static final int HRESULT_ALREADY_EXISTS = 0x800700B7;

    static final int EXTENDED_STARTUPINFO_PRESENT = 0x00080000;
    static final int CREATE_SUSPENDED = 0x00000004;
    static final int DETACHED_PROCESS = 0x00000008;
    static final int CREATE_UNICODE_ENVIRONMENT = 0x00000400;

    static final long PROC_THREAD_ATTRIBUTE_HANDLE_LIST = 0x00020002L;
    static final long PROC_THREAD_ATTRIBUTE_SECURITY_CAPABILITIES = 0x00020009L;
    static final long PROC_THREAD_ATTRIBUTE_CHILD_PROCESS_POLICY = 0x0002000EL;
    static final long PROC_THREAD_ATTRIBUTE_ALL_APPLICATION_PACKAGES_POLICY = 0x0002000FL;
    static final int PROCESS_CREATION_CHILD_PROCESS_RESTRICTED = 0x01;
    static final int PROCESS_CREATION_ALL_APPLICATION_PACKAGES_OPT_OUT = 0x01;

    static final int STARTF_USESTDHANDLES = 0x00000100;
    static final int HANDLE_FLAG_INHERIT = 0x00000001;
    static final int WAIT_TIMEOUT = 0x102;

    static final int JOB_OBJECT_LIMIT_PROCESS_TIME = 0x00000002;
    static final int JOB_OBJECT_LIMIT_ACTIVE_PROCESS = 0x00000008;
    static final int JOB_OBJECT_LIMIT_JOB_MEMORY = 0x00000200;
    static final int JOB_OBJECT_LIMIT_DIE_ON_UNHANDLED_EXCEPTION = 0x00000400;
    static final int JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x00002000;

    static final int JobObjectBasicAccountingInformation = 1;
    static final int JobObjectBasicUIRestrictions = 4;
    static final int JobObjectExtendedLimitInformation = 9;
    static final int JobObjectCpuRateControlInformation = 15;

    static final int JOB_OBJECT_UILIMIT_ALL = 0x000000FF;
    static final int JOB_OBJECT_CPU_RATE_CONTROL_ENABLE = 0x1;
    static final int JOB_OBJECT_CPU_RATE_CONTROL_HARD_CAP = 0x4;
    static final int SE_GROUP_ENABLED = 0x00000004;

    // Tailles des structures natives (x64).
    static final long SECURITY_CAPABILITIES_SIZE = 24;   // ptr sid @0, ptr caps @8, uint count @16, uint reserved @20
    static final long SID_AND_ATTRIBUTES_SIZE = 16;      // ptr sid @0, uint attrs @8
    static final long STARTUPINFOEX_SIZE = 112;          // cb@0 dwFlags@60 hStdIn@80 hStdOut@88 hStdErr@96 attrList@104
    static final long PROCESS_INFORMATION_SIZE = 24;     // hProcess@0 hThread@8
    static final long SECURITY_ATTRIBUTES_SIZE = 24;     // nLength@0 bInheritHandle(BOOL)@16
    static final long JOB_EXT_SIZE = 144;                // PerProcessUserTimeLimit@0 LimitFlags@16 ActiveProcessLimit@40 JobMemoryLimit@120 PeakJobMemoryUsed@136
    static final long JOB_ACCT_SIZE = 48;                // TotalUserTime@0

    private static final Arena G = Arena.global();
    private static final Linker L = Linker.nativeLinker();
    private static final StructLayout CS = Linker.Option.captureStateLayout();
    private static final VarHandle GLE = CS.varHandle(MemoryLayout.PathElement.groupElement("GetLastError"));
    private static final ThreadLocal<MemorySegment> CAP = ThreadLocal.withInitial(() -> G.allocate(CS));

    private static MethodHandle fn(String lib, String name, FunctionDescriptor fd, boolean err) {
        MemorySegment sym = SymbolLookup.libraryLookup(lib, G).find(name)
                .orElseThrow(() -> new UnsatisfiedLinkError(lib + "!" + name));
        return err ? L.downcallHandle(sym, fd, Linker.Option.captureCallState("GetLastError"))
                   : L.downcallHandle(sym, fd);
    }

    private static final MethodHandle CreateAppContainerProfile = fn("userenv.dll", "CreateAppContainerProfile",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS), false);
    private static final MethodHandle DeriveAppContainerSid = fn("userenv.dll", "DeriveAppContainerSidFromAppContainerName",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS), false);
    private static final MethodHandle ConvertSidToStringSidW = fn("advapi32.dll", "ConvertSidToStringSidW",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS), true);
    private static final MethodHandle FreeSid = fn("advapi32.dll", "FreeSid", FunctionDescriptor.of(ADDRESS, ADDRESS), false);
    private static final MethodHandle DeriveCapabilitySidsFromName = fn("kernelbase.dll", "DeriveCapabilitySidsFromName",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS), true);
    private static final MethodHandle LocalFree = fn("kernel32.dll", "LocalFree", FunctionDescriptor.of(ADDRESS, ADDRESS), false);
    private static final MethodHandle InitAttr = fn("kernel32.dll", "InitializeProcThreadAttributeList",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS), true);
    private static final MethodHandle UpdateAttr = fn("kernel32.dll", "UpdateProcThreadAttribute",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_LONG, ADDRESS, JAVA_LONG, ADDRESS, ADDRESS), true);
    private static final MethodHandle DeleteAttr = fn("kernel32.dll", "DeleteProcThreadAttributeList",
            FunctionDescriptor.ofVoid(ADDRESS), false);
    private static final MethodHandle CreateProcessW = fn("kernel32.dll", "CreateProcessW",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS), true);
    private static final MethodHandle CreatePipe = fn("kernel32.dll", "CreatePipe",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT), true);
    private static final MethodHandle SetHandleInformation = fn("kernel32.dll", "SetHandleInformation",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_INT, JAVA_INT), true);
    private static final MethodHandle CreateJobObjectW = fn("kernel32.dll", "CreateJobObjectW",
            FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS), true);
    private static final MethodHandle SetInformationJobObject = fn("kernel32.dll", "SetInformationJobObject",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_INT, ADDRESS, JAVA_INT), true);
    private static final MethodHandle QueryInformationJobObject = fn("kernel32.dll", "QueryInformationJobObject",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS), true);
    private static final MethodHandle AssignProcessToJobObject = fn("kernel32.dll", "AssignProcessToJobObject",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_LONG), true);
    private static final MethodHandle TerminateJobObject = fn("kernel32.dll", "TerminateJobObject",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_INT), false);
    private static final MethodHandle ResumeThread = fn("kernel32.dll", "ResumeThread",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG), false);
    private static final MethodHandle WaitForSingleObject = fn("kernel32.dll", "WaitForSingleObject",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_INT), false);
    private static final MethodHandle GetExitCodeProcess = fn("kernel32.dll", "GetExitCodeProcess",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, ADDRESS), false);
    private static final MethodHandle CloseHandle = fn("kernel32.dll", "CloseHandle",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG), false);
    private static final MethodHandle ReadFile = fn("kernel32.dll", "ReadFile",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, ADDRESS), true);
    private static final MethodHandle WriteFile = fn("kernel32.dll", "WriteFile",
            FunctionDescriptor.of(JAVA_INT, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, ADDRESS), true);

    /** Dernière erreur Win32 capturée par le dernier appel « avec erreur » de ce thread. */
    static int lastError() { return (int) GLE.get(CAP.get(), 0L); }

    static int createAppContainerProfile(MemorySegment name, MemorySegment disp, MemorySegment desc, MemorySegment outSid) throws Throwable {
        return (int) CreateAppContainerProfile.invokeExact(name, disp, desc, MemorySegment.NULL, 0, outSid);
    }
    static int deriveAppContainerSid(MemorySegment name, MemorySegment outSid) throws Throwable {
        return (int) DeriveAppContainerSid.invokeExact(name, outSid);
    }
    static boolean convertSidToStringSid(MemorySegment sid, MemorySegment out) throws Throwable {
        return (int) ConvertSidToStringSidW.invokeExact(CAP.get(), sid, out) != 0;
    }
    static void freeSid(MemorySegment sid) throws Throwable { MemorySegment r = (MemorySegment) FreeSid.invokeExact(sid); }
    static void localFree(MemorySegment p) throws Throwable { MemorySegment r = (MemorySegment) LocalFree.invokeExact(p); }
    static boolean deriveCapabilitySidsFromName(MemorySegment name, MemorySegment g, MemorySegment gc, MemorySegment cs, MemorySegment cc) throws Throwable {
        return (int) DeriveCapabilitySidsFromName.invokeExact(CAP.get(), name, g, gc, cs, cc) != 0;
    }
    static boolean initAttr(MemorySegment list, int count, MemorySegment size) throws Throwable {
        return (int) InitAttr.invokeExact(CAP.get(), list, count, 0, size) != 0;
    }
    static boolean updateAttr(MemorySegment list, long attr, MemorySegment value, long size) throws Throwable {
        return (int) UpdateAttr.invokeExact(CAP.get(), list, 0, attr, value, size, MemorySegment.NULL, MemorySegment.NULL) != 0;
    }
    static void deleteAttr(MemorySegment list) throws Throwable { DeleteAttr.invokeExact(list); }
    static boolean createProcess(MemorySegment app, MemorySegment cmd, boolean inherit, int flags, MemorySegment env,
                                 MemorySegment cwd, MemorySegment si, MemorySegment pi) throws Throwable {
        return (int) CreateProcessW.invokeExact(CAP.get(), app, cmd, MemorySegment.NULL, MemorySegment.NULL,
                inherit ? 1 : 0, flags, env, cwd, si, pi) != 0;
    }
    static boolean createPipe(MemorySegment r, MemorySegment w, MemorySegment sa) throws Throwable {
        return (int) CreatePipe.invokeExact(CAP.get(), r, w, sa, 0) != 0;
    }
    static void setHandleInformation(long h, int mask, int flags) throws Throwable {
        int r = (int) SetHandleInformation.invokeExact(CAP.get(), h, mask, flags);
    }
    static long createJobObject() throws Throwable {
        return (long) CreateJobObjectW.invokeExact(CAP.get(), MemorySegment.NULL, MemorySegment.NULL);
    }
    static boolean setInformationJobObject(long job, int cls, MemorySegment info, int size) throws Throwable {
        return (int) SetInformationJobObject.invokeExact(CAP.get(), job, cls, info, size) != 0;
    }
    static void queryInformationJobObject(long job, int cls, MemorySegment info, int size) throws Throwable {
        int r = (int) QueryInformationJobObject.invokeExact(CAP.get(), job, cls, info, size, MemorySegment.NULL);
    }
    static boolean assignProcessToJobObject(long job, long process) throws Throwable {
        return (int) AssignProcessToJobObject.invokeExact(CAP.get(), job, process) != 0;
    }
    static void terminateJobObject(long job, int code) throws Throwable { int r = (int) TerminateJobObject.invokeExact(job, code); }
    static void resumeThread(long t) throws Throwable { int r = (int) ResumeThread.invokeExact(t); }
    static int waitForSingleObject(long h, int ms) throws Throwable { return (int) WaitForSingleObject.invokeExact(h, ms); }
    static int getExitCodeProcess(long h, MemorySegment out) throws Throwable {
        int r = (int) GetExitCodeProcess.invokeExact(h, out);
        return out.get(JAVA_INT, 0);
    }
    static void closeHandle(long h) throws Throwable { if (h != 0) { int r = (int) CloseHandle.invokeExact(h); } }
    /** @return octets lus, ou -1 en fin de flux / erreur. */
    static int readFile(long h, MemorySegment buf, int len, MemorySegment nread) throws Throwable {
        int ok = (int) ReadFile.invokeExact(CAP.get(), h, buf, len, nread, MemorySegment.NULL);
        return ok == 0 ? -1 : nread.get(JAVA_INT, 0);
    }
    static boolean writeFile(long h, MemorySegment buf, int len, MemorySegment nwritten) throws Throwable {
        return (int) WriteFile.invokeExact(CAP.get(), h, buf, len, nwritten, MemorySegment.NULL) != 0;
    }
}
