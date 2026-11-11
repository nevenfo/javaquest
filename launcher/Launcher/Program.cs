using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace JavaQuest.Launcher
{
    /// <summary>
    /// JavaQuest.exe : démarre le serveur local (runtime Java embarqué), ouvre le jeu dans le navigateur,
    /// et arrête tout proprement à la fermeture. Instance unique par dossier d'installation.
    /// Disposition attendue : &lt;dossier&gt;\JavaQuest.exe et &lt;dossier&gt;\app\{runtime,server.jar,runner\runner.jar,bin\jq-sandbox.exe,web,content}.
    /// </summary>
    internal static class Program
    {
        [STAThread]
        private static int Main(string[] args)
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            string baseDir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd('\\');
            string appDir = Path.Combine(baseDir, "app");
            bool noBrowser = Array.IndexOf(args, "--no-browser") >= 0;
            for (int i = 0; i + 1 < args.Length; i++) if (args[i] == "--app") appDir = Path.GetFullPath(args[i + 1]);

            string dataDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "JavaQuest");
            Directory.CreateDirectory(dataDir);
            string key = Hash(appDir.ToLowerInvariant());
            string sessionFile = Path.Combine(dataDir, "session-" + key + ".txt");

            bool created;
            using (var mutex = new Mutex(true, "Local\\JavaQuest-" + key, out created))
            {
                if (!created)
                {
                    // Déjà lancé : on rouvre simplement le jeu.
                    string url = File.Exists(sessionFile) ? File.ReadAllText(sessionFile).Trim() : null;
                    if (!string.IsNullOrEmpty(url)) { if (!noBrowser) OpenBrowser(url); }
                    else MessageBox.Show("JavaQuest est déjà en cours de démarrage.", "JavaQuest", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    return 0;
                }
                var server = new ServerProcess(appDir, dataDir);
                string error = server.Start();
                if (error != null)
                {
                    MessageBox.Show(error + "\n\nJournal : " + server.LogPath, "JavaQuest — démarrage impossible", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    server.Stop();
                    return 1;
                }
                File.WriteAllText(sessionFile, server.Url);
                if (!noBrowser) OpenBrowser(server.Url);
                Application.Run(new MainForm(server));
                server.Stop();
                try { File.Delete(sessionFile); } catch (IOException) { }
                return 0;
            }
        }

        internal static void OpenBrowser(string url)
        {
            try { Process.Start(new ProcessStartInfo(url) { UseShellExecute = true }); }
            catch (Exception e) { MessageBox.Show("Impossible d'ouvrir le navigateur : " + e.Message + "\nOuvre manuellement : " + url, "JavaQuest"); }
        }

        private static string Hash(string s)
        {
            using (var sha = SHA256.Create())
            {
                byte[] h = sha.ComputeHash(Encoding.UTF8.GetBytes(s));
                return BitConverter.ToString(h, 0, 6).Replace("-", "").ToLowerInvariant();
            }
        }
    }

    /// <summary>Processus serveur rattaché à un Job « kill-on-close » : il meurt avec le lanceur.</summary>
    internal sealed class ServerProcess
    {
        private readonly string _app, _data;
        private Process _proc;
        private IntPtr _job;
        public string Url { get; private set; }
        public string LogPath { get; private set; }

        public ServerProcess(string app, string data)
        {
            _app = app; _data = data;
            LogPath = Path.Combine(data, "javaquest.log");
        }

        public string Start()
        {
            string java = Path.Combine(_app, "runtime", "bin", "javaw.exe");
            string jar = Path.Combine(_app, "server.jar");
            if (!File.Exists(java) || !File.Exists(jar))
                return "Fichiers du jeu introuvables dans « " + _app + " ».\nExtrais tout le contenu du ZIP (pas seulement JavaQuest.exe) puis relance.";
            if (Environment.OSVersion.Version.Major < 10)
                return "JavaQuest nécessite Windows 10 ou 11 (isolation AppContainer).";

            _job = Native.CreateKillOnCloseJob();
            var psi = new ProcessStartInfo(java)
            {
                Arguments = "-Xmx256m -XX:+UseSerialGC -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar " + Q(jar)
                            + " --app " + Q(_app) + " --data " + Q(_data) + " --watch-stdin 1",
                UseShellExecute = false, CreateNoWindow = true,
                RedirectStandardInput = true, RedirectStandardOutput = true, RedirectStandardError = true,
                WorkingDirectory = _app,
                StandardOutputEncoding = Encoding.UTF8, StandardErrorEncoding = Encoding.UTF8
            };
            try { _proc = Process.Start(psi); }
            catch (Exception e) { return "Le runtime Java n'a pas pu démarrer : " + e.Message; }
            if (_job != IntPtr.Zero) Native.AssignProcessToJobObject(_job, _proc.Handle);

            var log = new StreamWriter(LogPath, true, new UTF8Encoding(false)) { AutoFlush = true };
            log.WriteLine("---- " + DateTime.Now.ToString("s") + " démarrage " + _app);
            _proc.ErrorDataReceived += (s, e) => { if (e.Data != null) lock (log) log.WriteLine(e.Data); };
            _proc.BeginErrorReadLine();

            var ready = new ManualResetEvent(false);
            var reader = new Thread(() =>
            {
                string line;
                while ((line = _proc.StandardOutput.ReadLine()) != null)
                {
                    if (line.StartsWith("JAVAQUEST_READY ")) { Url = line.Substring(16).Trim(); ready.Set(); }
                    else lock (log) log.WriteLine(line);
                }
                ready.Set();
            }) { IsBackground = true };
            reader.Start();
            if (!ready.WaitOne(60000) || Url == null)
                return "Le serveur local ne s'est pas lancé (délai dépassé ou arrêt prématuré).";
            return null;
        }

        public void Stop()
        {
            try
            {
                if (_proc != null && !_proc.HasExited)
                {
                    _proc.StandardInput.Close(); // arrêt propre : le serveur surveille stdin
                    if (!_proc.WaitForExit(5000)) _proc.Kill();
                }
            }
            catch (Exception) { }
            if (_job != IntPtr.Zero) { Native.CloseHandle(_job); _job = IntPtr.Zero; } // tue tout reste éventuel
        }

        private static string Q(string s) { return "\"" + s.TrimEnd('\\') + "\""; }
    }

    internal sealed class MainForm : Form
    {
        public MainForm(ServerProcess server)
        {
            Text = "JavaQuest";
            FormBorderStyle = FormBorderStyle.FixedSingle;
            MaximizeBox = false;
            StartPosition = FormStartPosition.CenterScreen;
            ClientSize = new Size(420, 170);
            BackColor = Color.FromArgb(38, 33, 51);
            Font = new Font("Segoe UI", 10f);
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch (Exception) { }

            var title = new Label { Text = "JavaQuest est lancé", ForeColor = Color.FromArgb(255, 214, 120), Font = new Font("Segoe UI", 14f, FontStyle.Bold), AutoSize = true, Location = new Point(20, 16) };
            var info = new Label { Text = "Le jeu s'ouvre dans ton navigateur. Garde cette fenêtre ouverte\npendant que tu joues ; ferme-la pour arrêter JavaQuest.", ForeColor = Color.Gainsboro, AutoSize = true, Location = new Point(20, 52) };
            var open = new Button { Text = "Ouvrir le jeu", Location = new Point(20, 112), Size = new Size(170, 38), FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(92, 160, 92), ForeColor = Color.White };
            var stop = new Button { Text = "Arrêter JavaQuest", Location = new Point(230, 112), Size = new Size(170, 38), FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(150, 70, 70), ForeColor = Color.White };
            open.Click += (s, e) => Program.OpenBrowser(server.Url);
            stop.Click += (s, e) => Close();
            Controls.AddRange(new Control[] { title, info, open, stop });
        }
    }

    internal static class Native
    {
        [StructLayout(LayoutKind.Sequential)]
        private struct BASIC { public long a, b; public uint LimitFlags; public UIntPtr c, d; public uint e; public UIntPtr f; public uint g, h; }
        [StructLayout(LayoutKind.Sequential)]
        private struct IO { public ulong a, b, c, d, e, f; }
        [StructLayout(LayoutKind.Sequential)]
        private struct EXT { public BASIC Basic; public IO Io; public UIntPtr p, j, pp, pj; }

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] private static extern IntPtr CreateJobObject(IntPtr a, string n);
        [DllImport("kernel32.dll")] private static extern bool SetInformationJobObject(IntPtr job, int cls, ref EXT info, int size);
        [DllImport("kernel32.dll")] public static extern bool AssignProcessToJobObject(IntPtr job, IntPtr proc);
        [DllImport("kernel32.dll")] public static extern bool CloseHandle(IntPtr h);

        public static IntPtr CreateKillOnCloseJob()
        {
            IntPtr job = CreateJobObject(IntPtr.Zero, null);
            if (job == IntPtr.Zero) return IntPtr.Zero;
            var ext = new EXT();
            ext.Basic.LimitFlags = 0x2000; // JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE
            SetInformationJobObject(job, 9, ref ext, Marshal.SizeOf(typeof(EXT)));
            return job;
        }
    }
}
