using System;
using System.Runtime.InteropServices;
using Microsoft.Win32.SafeHandles;

namespace JavaQuest.Sandbox
{
    /// <summary>Déclarations Win32 minimales (AppContainer, Job Objects, processus, tubes).</summary>
    internal static class Native
    {
        public const int S_OK = 0;
        public const int HRESULT_ALREADY_EXISTS = unchecked((int)0x800700B7);

        public const uint EXTENDED_STARTUPINFO_PRESENT = 0x00080000;
        public const uint CREATE_SUSPENDED = 0x00000004;
        public const uint CREATE_NO_WINDOW = 0x08000000;
        public const uint DETACHED_PROCESS = 0x00000008;
        public const uint CREATE_UNICODE_ENVIRONMENT = 0x00000400;
        public const uint CREATE_BREAKAWAY_FROM_JOB = 0x01000000;

        public static readonly IntPtr PROC_THREAD_ATTRIBUTE_HANDLE_LIST = (IntPtr)0x00020002;
        public static readonly IntPtr PROC_THREAD_ATTRIBUTE_SECURITY_CAPABILITIES = (IntPtr)0x00020009;
        public static readonly IntPtr PROC_THREAD_ATTRIBUTE_CHILD_PROCESS_POLICY = (IntPtr)0x0002000E;
        public static readonly IntPtr PROC_THREAD_ATTRIBUTE_ALL_APPLICATION_PACKAGES_POLICY = (IntPtr)0x0002000F;
        public const uint PROCESS_CREATION_CHILD_PROCESS_RESTRICTED = 0x01;
        public const uint PROCESS_CREATION_ALL_APPLICATION_PACKAGES_OPT_OUT = 0x01;

        public const uint STARTF_USESTDHANDLES = 0x00000100;
        public const uint HANDLE_FLAG_INHERIT = 0x00000001;
        public const uint INFINITE = 0xFFFFFFFF;
        public const uint WAIT_OBJECT_0 = 0;
        public const uint WAIT_TIMEOUT = 0x102;

        // Job limits
        public const uint JOB_OBJECT_LIMIT_PROCESS_TIME = 0x00000002;
        public const uint JOB_OBJECT_LIMIT_ACTIVE_PROCESS = 0x00000008;
        public const uint JOB_OBJECT_LIMIT_JOB_MEMORY = 0x00000200;
        public const uint JOB_OBJECT_LIMIT_DIE_ON_UNHANDLED_EXCEPTION = 0x00000400;
        public const uint JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x00002000;

        public const int JobObjectBasicAccountingInformation = 1;
        public const int JobObjectBasicUIRestrictions = 4;
        public const int JobObjectExtendedLimitInformation = 9;
        public const int JobObjectCpuRateControlInformation = 15;

        public const uint JOB_OBJECT_UILIMIT_ALL = 0x000000FF;
        public const uint JOB_OBJECT_CPU_RATE_CONTROL_ENABLE = 0x1;
        public const uint JOB_OBJECT_CPU_RATE_CONTROL_HARD_CAP = 0x4;

        [StructLayout(LayoutKind.Sequential)]
        public struct SECURITY_CAPABILITIES
        {
            public IntPtr AppContainerSid;
            public IntPtr Capabilities;
            public uint CapabilityCount;
            public uint Reserved;
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
        public struct STARTUPINFO
        {
            public int cb;
            public string lpReserved;
            public string lpDesktop;
            public string lpTitle;
            public int dwX, dwY, dwXSize, dwYSize, dwXCountChars, dwYCountChars, dwFillAttribute;
            public uint dwFlags;
            public short wShowWindow;
            public short cbReserved2;
            public IntPtr lpReserved2;
            public IntPtr hStdInput, hStdOutput, hStdError;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct STARTUPINFOEX
        {
            public STARTUPINFO StartupInfo;
            public IntPtr lpAttributeList;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct PROCESS_INFORMATION
        {
            public IntPtr hProcess, hThread;
            public int dwProcessId, dwThreadId;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct SECURITY_ATTRIBUTES
        {
            public int nLength;
            public IntPtr lpSecurityDescriptor;
            public bool bInheritHandle;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct JOBOBJECT_BASIC_LIMIT_INFORMATION
        {
            public long PerProcessUserTimeLimit;
            public long PerJobUserTimeLimit;
            public uint LimitFlags;
            public UIntPtr MinimumWorkingSetSize;
            public UIntPtr MaximumWorkingSetSize;
            public uint ActiveProcessLimit;
            public UIntPtr Affinity;
            public uint PriorityClass;
            public uint SchedulingClass;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct IO_COUNTERS
        {
            public ulong ReadOperationCount, WriteOperationCount, OtherOperationCount;
            public ulong ReadTransferCount, WriteTransferCount, OtherTransferCount;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct JOBOBJECT_EXTENDED_LIMIT_INFORMATION
        {
            public JOBOBJECT_BASIC_LIMIT_INFORMATION BasicLimitInformation;
            public IO_COUNTERS IoInfo;
            public UIntPtr ProcessMemoryLimit;
            public UIntPtr JobMemoryLimit;
            public UIntPtr PeakProcessMemoryUsed;
            public UIntPtr PeakJobMemoryUsed;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct JOBOBJECT_BASIC_ACCOUNTING_INFORMATION
        {
            public long TotalUserTime, TotalKernelTime, ThisPeriodTotalUserTime, ThisPeriodTotalKernelTime;
            public uint TotalPageFaultCount, TotalProcesses, ActiveProcesses, TotalTerminatedProcesses;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct JOBOBJECT_BASIC_UI_RESTRICTIONS { public uint UIRestrictionsClass; }

        [StructLayout(LayoutKind.Sequential)]
        public struct JOBOBJECT_CPU_RATE_CONTROL_INFORMATION { public uint ControlFlags; public uint CpuRate; }

        [DllImport("userenv.dll", CharSet = CharSet.Unicode)]
        public static extern int CreateAppContainerProfile(string name, string displayName, string description,
            IntPtr capabilities, uint capabilityCount, out IntPtr sid);

        [DllImport("userenv.dll", CharSet = CharSet.Unicode)]
        public static extern int DeriveAppContainerSidFromAppContainerName(string name, out IntPtr sid);

        [DllImport("userenv.dll", CharSet = CharSet.Unicode)]
        public static extern int DeleteAppContainerProfile(string name);

        [DllImport("advapi32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        public static extern bool ConvertSidToStringSid(IntPtr sid, out IntPtr stringSid);

        [DllImport("advapi32.dll")]
        public static extern IntPtr FreeSid(IntPtr sid);

        [DllImport("kernelbase.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        public static extern bool DeriveCapabilitySidsFromName(string capName, out IntPtr groupSids, out uint groupCount,
            out IntPtr capSids, out uint capCount);

        [StructLayout(LayoutKind.Sequential)]
        public struct SID_AND_ATTRIBUTES { public IntPtr Sid; public uint Attributes; }
        public const uint SE_GROUP_ENABLED = 0x00000004;

        [DllImport("kernel32.dll")]
        public static extern IntPtr LocalFree(IntPtr mem);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool InitializeProcThreadAttributeList(IntPtr list, int count, int flags, ref IntPtr size);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool UpdateProcThreadAttribute(IntPtr list, uint flags, IntPtr attribute, IntPtr value,
            IntPtr size, IntPtr previous, IntPtr returnSize);

        [DllImport("kernel32.dll")]
        public static extern void DeleteProcThreadAttributeList(IntPtr list);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        public static extern bool CreateProcess(string app, System.Text.StringBuilder cmd, IntPtr procAttr, IntPtr threadAttr,
            bool inherit, uint flags, IntPtr env, string cwd, ref STARTUPINFOEX si, out PROCESS_INFORMATION pi);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool CreatePipe(out SafeFileHandle read, out SafeFileHandle write, ref SECURITY_ATTRIBUTES sa, int size);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool SetHandleInformation(SafeHandle h, uint mask, uint flags);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        public static extern IntPtr CreateJobObject(IntPtr attr, string name);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool SetInformationJobObject(IntPtr job, int cls, IntPtr info, int size);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool QueryInformationJobObject(IntPtr job, int cls, IntPtr info, int size, IntPtr ret);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool AssignProcessToJobObject(IntPtr job, IntPtr process);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool TerminateJobObject(IntPtr job, uint exitCode);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern uint ResumeThread(IntPtr thread);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern uint WaitForSingleObject(IntPtr h, uint ms);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool GetExitCodeProcess(IntPtr h, out uint code);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool CloseHandle(IntPtr h);
    }
}
