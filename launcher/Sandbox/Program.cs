using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using Microsoft.Win32.SafeHandles;

namespace JavaQuest.Sandbox
{
    /// <summary>
    /// jq-sandbox.exe — exécute un programme NON FIABLE dans :
    ///  - un AppContainer sans aucune capacité (pas de réseau, pas d'accès aux fichiers de l'utilisateur,
    ///    écriture limitée au dossier du conteneur), optionnellement en mode LPAC ;
    ///  - un Job Object (1 seul processus, mémoire, temps CPU, plafond CPU, restrictions UI, kill-on-close) ;
    ///  - une politique noyau interdisant la création de processus enfants ;
    ///  - un délai mural et un plafond de sortie.
    /// Commandes :
    ///   jq-sandbox grant &lt;dossier&gt;          accorde lecture/exécution au conteneur (une fois)
    ///   jq-sandbox sid                      affiche le SID du conteneur
    ///   jq-sandbox run [options] -- exe args  exécute ; résultat JSON sur stdout
    /// Options run : --timeout-ms N --cpu-ms N --mem-mb N --max-out N --cwd D --stdin-file F --lpac
    /// </summary>
    internal static class Program
    {
        private const string ContainerName = "JavaQuest.Sandbox.v1";

        private static int Main(string[] args)
        {
            try
            {
                if (args.Length == 0) return Usage();
                switch (args[0])
                {
                    case "sid": Console.WriteLine(SidString(GetContainerSid())); return 0;
                    case "grant": return Grant(args);
                    case "run": return Run(args);
                    default: return Usage();
                }
            }
            catch (Exception e)
            {
                Console.Error.WriteLine("jq-sandbox: " + e.Message);
                return 3;
            }
        }

        private static int Usage()
        {
            Console.Error.WriteLine("usage: jq-sandbox grant <dir> | sid | run [--timeout-ms N --cpu-ms N --mem-mb N --max-out N --cwd D --stdin-file F --lpac] -- exe args...");
            return 2;
        }

        private static IntPtr GetContainerSid()
        {
            IntPtr sid;
            int hr = Native.CreateAppContainerProfile(ContainerName, "JavaQuest Sandbox",
                "Exécution isolée du code Java des joueurs JavaQuest", IntPtr.Zero, 0, out sid);
            if (hr == Native.HRESULT_ALREADY_EXISTS)
                hr = Native.DeriveAppContainerSidFromAppContainerName(ContainerName, out sid);
            if (hr != Native.S_OK) throw new Exception("AppContainer indisponible (HRESULT 0x" + hr.ToString("X8") + ")");
            return sid;
        }

        private static string SidString(IntPtr sid)
        {
            IntPtr str;
            if (!Native.ConvertSidToStringSid(sid, out str)) throw new Exception("ConvertSidToStringSid " + Marshal.GetLastWin32Error());
            try { return Marshal.PtrToStringUni(str); } finally { Native.LocalFree(str); }
        }

        /// <summary>Accorde RX (hérité) au SID du conteneur sur un dossier, via icacls (aucun privilège admin requis pour son propre dossier).</summary>
        private static int Grant(string[] args)
        {
            if (args.Length < 2) return Usage();
            string dir = Path.GetFullPath(args[1]);
            if (!Directory.Exists(dir)) throw new Exception("dossier introuvable : " + dir);
            string sid = SidString(GetContainerSid());
            var psi = new ProcessStartInfo(Path.Combine(Environment.SystemDirectory, "icacls.exe"),
                "\"" + dir.TrimEnd('\\') + "\" /grant *" + sid + ":(OI)(CI)RX /Q /C")
            { UseShellExecute = false, CreateNoWindow = true, RedirectStandardOutput = true, RedirectStandardError = true };
            using (var p = Process.Start(psi))
            {
                string err = p.StandardError.ReadToEnd();
                p.StandardOutput.ReadToEnd();
                p.WaitForExit();
                if (p.ExitCode != 0) { Console.Error.WriteLine(err); return 4; }
            }
            Console.WriteLine(sid);
            return 0;
        }

        private sealed class Options
        {
            public int TimeoutMs = 10000, CpuMs = 8000, MemMb = 768, MaxOut = 256 * 1024, CpuRate = 50;
            public string Cwd, StdinFile;
            public bool Lpac;
            public List<string> Caps = new List<string>();
            public List<string> Command = new List<string>();
        }

        private static Options Parse(string[] args)
        {
            var o = new Options();
            int i = 1;
            for (; i < args.Length; i++)
            {
                string a = args[i];
                if (a == "--") { i++; break; }
                switch (a)
                {
                    case "--timeout-ms": o.TimeoutMs = int.Parse(args[++i]); break;
                    case "--cpu-ms": o.CpuMs = int.Parse(args[++i]); break;
                    case "--mem-mb": o.MemMb = int.Parse(args[++i]); break;
                    case "--max-out": o.MaxOut = int.Parse(args[++i]); break;
                    case "--cpu-rate": o.CpuRate = int.Parse(args[++i]); break;
                    case "--cwd": o.Cwd = args[++i]; break;
                    case "--stdin-file": o.StdinFile = args[++i]; break;
                    case "--lpac": o.Lpac = true; break;
                    case "--cap": o.Caps.Add(args[++i]); break;
                    default: throw new Exception("option inconnue : " + a);
                }
            }
            for (; i < args.Length; i++) o.Command.Add(args[i]);
            if (o.Command.Count == 0) throw new Exception("commande manquante");
            return o;
        }

        private static string Quote(string s)
        {
            if (s.Length > 0 && s.IndexOfAny(new[] { ' ', '\t', '"' }) < 0) return s;
            var sb = new StringBuilder("\"");
            int bs = 0;
            foreach (char c in s)
            {
                if (c == '\\') { bs++; continue; }
                if (c == '"') { sb.Append('\\', bs * 2 + 1); sb.Append('"'); bs = 0; continue; }
                sb.Append('\\', bs); bs = 0; sb.Append(c);
            }
            sb.Append('\\', bs * 2).Append('"');
            return sb.ToString();
        }

        private static IntPtr StructPtr<T>(T value)
        {
            IntPtr p = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(T)));
            Marshal.StructureToPtr(value, p, false);
            return p;
        }

        private static void SetJob<T>(IntPtr job, int cls, T info)
        {
            IntPtr p = StructPtr(info);
            try
            {
                if (!Native.SetInformationJobObject(job, cls, p, Marshal.SizeOf(typeof(T))))
                    throw new Exception("SetInformationJobObject(" + cls + ") " + Marshal.GetLastWin32Error());
            }
            finally { Marshal.FreeHGlobal(p); }
        }

        private static IntPtr CreateLimitedJob(Options o)
        {
            IntPtr job = Native.CreateJobObject(IntPtr.Zero, null);
            if (job == IntPtr.Zero) throw new Exception("CreateJobObject " + Marshal.GetLastWin32Error());
            var ext = new Native.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
            ext.BasicLimitInformation.LimitFlags = Native.JOB_OBJECT_LIMIT_ACTIVE_PROCESS | Native.JOB_OBJECT_LIMIT_JOB_MEMORY
                | Native.JOB_OBJECT_LIMIT_PROCESS_TIME | Native.JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE
                | Native.JOB_OBJECT_LIMIT_DIE_ON_UNHANDLED_EXCEPTION;
            ext.BasicLimitInformation.ActiveProcessLimit = 1;
            ext.BasicLimitInformation.PerProcessUserTimeLimit = (long)o.CpuMs * 10000L;
            ext.JobMemoryLimit = (UIntPtr)((ulong)o.MemMb * 1024UL * 1024UL);
            SetJob(job, Native.JobObjectExtendedLimitInformation, ext);
            SetJob(job, Native.JobObjectBasicUIRestrictions, new Native.JOBOBJECT_BASIC_UI_RESTRICTIONS { UIRestrictionsClass = Native.JOB_OBJECT_UILIMIT_ALL });
            if (o.CpuRate > 0 && o.CpuRate < 100)
            {
                SetJob(job, Native.JobObjectCpuRateControlInformation, new Native.JOBOBJECT_CPU_RATE_CONTROL_INFORMATION
                {
                    ControlFlags = Native.JOB_OBJECT_CPU_RATE_CONTROL_ENABLE | Native.JOB_OBJECT_CPU_RATE_CONTROL_HARD_CAP,
                    CpuRate = (uint)(o.CpuRate * 100)
                });
            }
            return job;
        }

        private sealed class Capture
        {
            private readonly Stream _s; private readonly int _max; private readonly MemoryStream _buf = new MemoryStream();
            public volatile bool Overflow; public readonly Thread Thread; private readonly Action _onOverflow;
            public Capture(Stream s, int max, Action onOverflow)
            {
                _s = s; _max = max; _onOverflow = onOverflow;
                Thread = new Thread(Loop) { IsBackground = true }; Thread.Start();
            }
            private void Loop()
            {
                var b = new byte[8192];
                try
                {
                    int n;
                    while ((n = _s.Read(b, 0, b.Length)) > 0)
                    {
                        int room = _max - (int)_buf.Length;
                        if (room > 0) _buf.Write(b, 0, Math.Min(room, n));
                        if (n > room && !Overflow) { Overflow = true; _onOverflow(); }
                    }
                }
                catch (IOException) { }
                catch (ObjectDisposedException) { }
            }
            public string Text() { return new UTF8Encoding(false).GetString(_buf.ToArray()); }
        }

        private static int Run(string[] args)
        {
            Options o = Parse(args);
            string exe = Path.GetFullPath(o.Command[0]);
            var cmd = new StringBuilder();
            foreach (string a in o.Command) { if (cmd.Length > 0) cmd.Append(' '); cmd.Append(Quote(a)); }

            IntPtr sid = GetContainerSid();
            var sa = new Native.SECURITY_ATTRIBUTES { nLength = Marshal.SizeOf(typeof(Native.SECURITY_ATTRIBUTES)), bInheritHandle = true };
            SafeFileHandle inR, inW, outR, outW, errR, errW;
            if (!Native.CreatePipe(out inR, out inW, ref sa, 0) || !Native.CreatePipe(out outR, out outW, ref sa, 0)
                || !Native.CreatePipe(out errR, out errW, ref sa, 0)) throw new Exception("CreatePipe " + Marshal.GetLastWin32Error());
            Native.SetHandleInformation(inW, Native.HANDLE_FLAG_INHERIT, 0);
            Native.SetHandleInformation(outR, Native.HANDLE_FLAG_INHERIT, 0);
            Native.SetHandleInformation(errR, Native.HANDLE_FLAG_INHERIT, 0);

            // Liste d'attributs : capacités de sécurité (AppContainer), liste blanche de handles, interdiction d'enfants, LPAC.
            int attrCount = o.Lpac ? 4 : 3;
            IntPtr size = IntPtr.Zero;
            Native.InitializeProcThreadAttributeList(IntPtr.Zero, attrCount, 0, ref size);
            IntPtr attrList = Marshal.AllocHGlobal(size);
            var toFree = new List<IntPtr> { attrList };
            if (!Native.InitializeProcThreadAttributeList(attrList, attrCount, 0, ref size)) throw new Exception("InitializeProcThreadAttributeList " + Marshal.GetLastWin32Error());
            IntPtr job = IntPtr.Zero;
            var pi = new Native.PROCESS_INFORMATION();
            try
            {
                IntPtr capArray = IntPtr.Zero;
                if (o.Caps.Count > 0)
                {
                    int sz = Marshal.SizeOf(typeof(Native.SID_AND_ATTRIBUTES));
                    capArray = Marshal.AllocHGlobal(sz * o.Caps.Count); toFree.Add(capArray);
                    for (int c = 0; c < o.Caps.Count; c++)
                    {
                        IntPtr g, cs; uint gc, cc;
                        if (!Native.DeriveCapabilitySidsFromName(o.Caps[c], out g, out gc, out cs, out cc) || cc < 1)
                            throw new Exception("capacité inconnue : " + o.Caps[c]);
                        Marshal.StructureToPtr(new Native.SID_AND_ATTRIBUTES { Sid = Marshal.ReadIntPtr(cs), Attributes = Native.SE_GROUP_ENABLED },
                            capArray + c * sz, false);
                    }
                }
                var caps = new Native.SECURITY_CAPABILITIES { AppContainerSid = sid, Capabilities = capArray, CapabilityCount = (uint)o.Caps.Count };
                IntPtr pCaps = StructPtr(caps); toFree.Add(pCaps);
                if (!Native.UpdateProcThreadAttribute(attrList, 0, Native.PROC_THREAD_ATTRIBUTE_SECURITY_CAPABILITIES, pCaps,
                    (IntPtr)Marshal.SizeOf(typeof(Native.SECURITY_CAPABILITIES)), IntPtr.Zero, IntPtr.Zero))
                    throw new Exception("attribut SECURITY_CAPABILITIES " + Marshal.GetLastWin32Error());

                IntPtr handles = Marshal.AllocHGlobal(IntPtr.Size * 3); toFree.Add(handles);
                Marshal.WriteIntPtr(handles, 0, inR.DangerousGetHandle());
                Marshal.WriteIntPtr(handles, IntPtr.Size, outW.DangerousGetHandle());
                Marshal.WriteIntPtr(handles, IntPtr.Size * 2, errW.DangerousGetHandle());
                if (!Native.UpdateProcThreadAttribute(attrList, 0, Native.PROC_THREAD_ATTRIBUTE_HANDLE_LIST, handles,
                    (IntPtr)(IntPtr.Size * 3), IntPtr.Zero, IntPtr.Zero))
                    throw new Exception("attribut HANDLE_LIST " + Marshal.GetLastWin32Error());

                IntPtr childPolicy = Marshal.AllocHGlobal(4); toFree.Add(childPolicy);
                Marshal.WriteInt32(childPolicy, (int)Native.PROCESS_CREATION_CHILD_PROCESS_RESTRICTED);
                if (!Native.UpdateProcThreadAttribute(attrList, 0, Native.PROC_THREAD_ATTRIBUTE_CHILD_PROCESS_POLICY, childPolicy,
                    (IntPtr)4, IntPtr.Zero, IntPtr.Zero))
                    throw new Exception("attribut CHILD_PROCESS_POLICY " + Marshal.GetLastWin32Error());

                if (o.Lpac)
                {
                    IntPtr lpac = Marshal.AllocHGlobal(4); toFree.Add(lpac);
                    Marshal.WriteInt32(lpac, (int)Native.PROCESS_CREATION_ALL_APPLICATION_PACKAGES_OPT_OUT);
                    if (!Native.UpdateProcThreadAttribute(attrList, 0, Native.PROC_THREAD_ATTRIBUTE_ALL_APPLICATION_PACKAGES_POLICY, lpac,
                        (IntPtr)4, IntPtr.Zero, IntPtr.Zero))
                        throw new Exception("attribut LPAC " + Marshal.GetLastWin32Error());
                }

                // Environnement minimal : seules les variables système/profil exigées par Windows pour
                // l'AppContainer sont transmises (aucun jeton, PATH utilisateur ou variable personnelle).
                string sysRoot = Environment.GetEnvironmentVariable("SystemRoot") ?? @"C:\Windows";
                var envSb = new StringBuilder();
                foreach (string k in new[] { "SystemRoot", "SystemDrive", "windir", "USERPROFILE", "LOCALAPPDATA", "APPDATA", "TEMP", "TMP", "NUMBER_OF_PROCESSORS", "PROCESSOR_ARCHITECTURE" })
                {
                    string v = Environment.GetEnvironmentVariable(k);
                    if (!string.IsNullOrEmpty(v)) envSb.Append(k).Append('=').Append(v).Append('\0');
                }
                envSb.Append("PATH=").Append(Path.Combine(sysRoot, "System32")).Append('\0').Append('\0');
                string env = envSb.ToString();
                IntPtr pEnv = Marshal.StringToHGlobalUni(env); toFree.Add(pEnv);

                var si = new Native.STARTUPINFOEX();
                si.StartupInfo.cb = Marshal.SizeOf(typeof(Native.STARTUPINFOEX));
                si.StartupInfo.dwFlags = Native.STARTF_USESTDHANDLES;
                si.StartupInfo.hStdInput = inR.DangerousGetHandle();
                si.StartupInfo.hStdOutput = outW.DangerousGetHandle();
                si.StartupInfo.hStdError = errW.DangerousGetHandle();
                si.lpAttributeList = attrList;

                job = CreateLimitedJob(o);
                string cwd = o.Cwd ?? Path.GetDirectoryName(exe);
                if (!Native.CreateProcess(exe, cmd, IntPtr.Zero, IntPtr.Zero, true,
                        Native.EXTENDED_STARTUPINFO_PRESENT | Native.CREATE_SUSPENDED | Native.DETACHED_PROCESS | Native.CREATE_UNICODE_ENVIRONMENT,
                        pEnv, cwd, ref si, out pi))
                    throw new Exception("CreateProcess " + Marshal.GetLastWin32Error());
                if (!Native.AssignProcessToJobObject(job, pi.hProcess))
                {
                    int err = Marshal.GetLastWin32Error();
                    Native.TerminateJobObject(job, 1);
                    throw new Exception("AssignProcessToJobObject " + err);
                }
                inR.Dispose(); outW.Dispose(); errW.Dispose();

                IntPtr jobForKill = job;
                Action kill = () => Native.TerminateJobObject(jobForKill, 0xC0000409);
                var outCap = new Capture(new FileStream(outR, FileAccess.Read, 4096, false), o.MaxOut, kill);
                var errCap = new Capture(new FileStream(errR, FileAccess.Read, 4096, false), o.MaxOut, kill);

                var sw = Stopwatch.StartNew();
                Native.ResumeThread(pi.hThread);

                var stdin = new FileStream(inW, FileAccess.Write, 4096, false);
                var feeder = new Thread(() =>
                {
                    try
                    {
                        if (o.StdinFile != null) { using (var f = File.OpenRead(o.StdinFile)) f.CopyTo(stdin); }
                        else Console.OpenStandardInput().CopyTo(stdin);
                    }
                    catch (IOException) { }
                    finally { try { stdin.Dispose(); } catch (IOException) { } }
                }) { IsBackground = true };
                feeder.Start();

                bool timedOut = Native.WaitForSingleObject(pi.hProcess, (uint)o.TimeoutMs) == Native.WAIT_TIMEOUT;
                if (timedOut) { Native.TerminateJobObject(job, 0xC0000420); Native.WaitForSingleObject(pi.hProcess, 3000); }
                sw.Stop();
                uint exit; Native.GetExitCodeProcess(pi.hProcess, out exit);
                outCap.Thread.Join(2000); errCap.Thread.Join(2000);

                var ext = new Native.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
                IntPtr pExt = StructPtr(ext); toFree.Add(pExt);
                Native.QueryInformationJobObject(job, Native.JobObjectExtendedLimitInformation, pExt, Marshal.SizeOf(typeof(Native.JOBOBJECT_EXTENDED_LIMIT_INFORMATION)), IntPtr.Zero);
                ext = (Native.JOBOBJECT_EXTENDED_LIMIT_INFORMATION)Marshal.PtrToStructure(pExt, typeof(Native.JOBOBJECT_EXTENDED_LIMIT_INFORMATION));
                var acct = new Native.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION();
                IntPtr pAcct = StructPtr(acct); toFree.Add(pAcct);
                Native.QueryInformationJobObject(job, Native.JobObjectBasicAccountingInformation, pAcct, Marshal.SizeOf(typeof(Native.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION)), IntPtr.Zero);
                acct = (Native.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION)Marshal.PtrToStructure(pAcct, typeof(Native.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION));

                ulong peak = ext.PeakJobMemoryUsed.ToUInt64();
                long cpuMs = acct.TotalUserTime / 10000;
                bool memLimit = peak >= (ulong)o.MemMb * 1024UL * 1024UL * 95UL / 100UL;
                bool cpuLimit = cpuMs >= o.CpuMs || exit == 0xC0000044 /*STATUS_QUOTA_EXCEEDED*/ || exit == 0x705; /*ERROR_NOT_ENOUGH_QUOTA*/

                var json = new StringBuilder("{");
                json.Append("\"exitCode\":").Append(unchecked((int)exit));
                json.Append(",\"timedOut\":").Append(timedOut ? "true" : "false");
                json.Append(",\"outputLimit\":").Append(outCap.Overflow || errCap.Overflow ? "true" : "false");
                json.Append(",\"memoryLimit\":").Append(memLimit ? "true" : "false");
                json.Append(",\"cpuLimit\":").Append(cpuLimit ? "true" : "false");
                json.Append(",\"wallMs\":").Append(sw.ElapsedMilliseconds);
                json.Append(",\"cpuMs\":").Append(cpuMs);
                json.Append(",\"peakMemMb\":").Append(peak / (1024 * 1024));
                json.Append(",\"lpac\":").Append(o.Lpac ? "true" : "false");
                json.Append(",\"stdout\":").Append(JsonStr(outCap.Text()));
                json.Append(",\"stderr\":").Append(JsonStr(errCap.Text()));
                json.Append("}");
                var stdout = new StreamWriter(Console.OpenStandardOutput(), new UTF8Encoding(false));
                stdout.Write(json.ToString()); stdout.Flush();
                return 0;
            }
            finally
            {
                if (pi.hThread != IntPtr.Zero) Native.CloseHandle(pi.hThread);
                if (pi.hProcess != IntPtr.Zero) Native.CloseHandle(pi.hProcess);
                if (job != IntPtr.Zero) { Native.TerminateJobObject(job, 1); Native.CloseHandle(job); }
                Native.DeleteProcThreadAttributeList(attrList);
                foreach (IntPtr p in toFree) Marshal.FreeHGlobal(p);
                Native.FreeSid(sid);
            }
        }

        private static string JsonStr(string s)
        {
            var sb = new StringBuilder("\"");
            foreach (char c in s)
            {
                switch (c)
                {
                    case '"': sb.Append("\\\""); break;
                    case '\\': sb.Append("\\\\"); break;
                    case '\n': sb.Append("\\n"); break;
                    case '\r': sb.Append("\\r"); break;
                    case '\t': sb.Append("\\t"); break;
                    default:
                        if (c < 0x20) sb.Append("\\u").Append(((int)c).ToString("x4"));
                        else sb.Append(c);
                        break;
                }
            }
            return sb.Append('"').ToString();
        }
    }
}
