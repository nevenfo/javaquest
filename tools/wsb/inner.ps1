# Exécuté DANS Windows Sandbox (Windows vierge, réseau désactivé) : installe le ZIP comme un étudiant et vérifie.
$ErrorActionPreference = "Continue"
$out = "C:\jq-out\result.txt"
function Log($m) { Add-Content -Path $out -Value $m -Encoding UTF8 }
Set-Content -Path $out -Value "START $(Get-Date -Format s)" -Encoding UTF8
try {
  Log ("OS " + [Environment]::OSVersion.VersionString)
  Log ("java dans le PATH : " + [bool](Get-Command java -ErrorAction SilentlyContinue))
  Log ("node dans le PATH : " + [bool](Get-Command node -ErrorAction SilentlyContinue))
  $net = $false
  try { $c = New-Object Net.Sockets.TcpClient; $net = $c.ConnectAsync("1.1.1.1", 443).Wait(3000) } catch { $net = $false }
  Log ("Internet joignable : " + $net)

  $dest = Join-Path $env:USERPROFILE "Desktop\Mes jeux é"
  New-Item -ItemType Directory -Force $dest | Out-Null
  Expand-Archive -Path "C:\jq-in\JavaQuest.zip" -DestinationPath $dest -Force
  $exe = Join-Path $dest "JavaQuest\JavaQuest.exe"
  Log ("ZIP extrait : " + (Test-Path $exe))

  $p = Start-Process -FilePath $exe -ArgumentList "--no-browser" -PassThru
  $sess = $null
  for ($i = 0; $i -lt 120 -and -not $sess; $i++) {
    Start-Sleep -Milliseconds 500
    $f = Get-ChildItem "$env:LOCALAPPDATA\JavaQuest\session-*.txt" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($f) { $sess = (Get-Content $f.FullName -Raw).Trim() }
  }
  Log ("Serveur démarré : " + [bool]$sess)
  if ($sess) {
    $base = $sess.Substring(0, $sess.IndexOf("/#t=")); $tok = $sess.Substring($sess.IndexOf("#t=") + 3)
    $h = Invoke-RestMethod "$base/api/health" -Headers @{ "X-JQ-Token" = $tok }
    Log ("Santé : ok=" + $h.ok + " sandbox=" + $h.sandbox + " " + $h.sandboxError)
    $body = '{"files":[{"name":"Main.java","content":"void main() { int s = 0; for (int i = 1; i <= 10; i++) s += i; IO.println(\"Somme \" + s); }"}]}'
    $r = Invoke-RestMethod "$base/api/run" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($body)) -Headers @{ "X-JQ-Token" = $tok } -ContentType "application/json"
    Log ("Java exécuté : status=" + $r.status + " stdout=" + ($r.stdout -replace "`n", "\n"))
    $body2 = '{"files":[{"name":"Main.java","content":"import java.nio.file.*; void main() throws Exception { IO.println(Files.readString(Path.of(System.getenv(\"USERPROFILE\"), \"Desktop\", \"secret.txt\"))); }"}]}'
    Set-Content -Path (Join-Path $env:USERPROFILE "Desktop\secret.txt") -Value "donnee-perso"
    $r2 = Invoke-RestMethod "$base/api/run" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($body2)) -Headers @{ "X-JQ-Token" = $tok } -ContentType "application/json"
    Log ("Lecture fichier perso depuis le code : status=" + $r2.status + " exception=" + $r2.exception.simpleType)
    $c = Invoke-RestMethod "$base/api/content" -Headers @{ "X-JQ-Token" = $tok }
    Log ("Quêtes chargées : " + $c.quests.Count)
    $idx = (Invoke-WebRequest "$base/" -UseBasicParsing).StatusCode
    Log ("Page du jeu : " + $idx)
  }
  $p.CloseMainWindow() | Out-Null
  $stopped = $p.WaitForExit(15000)
  Start-Sleep -Seconds 2
  $left = @(Get-Process javaw, java -ErrorAction SilentlyContinue).Count
  Log ("Arrêt propre : " + $stopped + " ; processus Java restants : " + $left)
} catch {
  Log ("ERREUR " + $_.Exception.Message)
}
# Diagnostic : signatures des exécutables livrés et derniers blocages Code Integrity (Smart App Control / WDAC).
if ($exe) {
  $d = Split-Path $exe
  foreach ($f in @("JavaQuest.exe", "app\bin\jq-sandbox.exe", "app\runtime\bin\javaw.exe")) { $sg = Get-AuthenticodeSignature (Join-Path $d $f); Log ("Signature $f : " + $sg.Status + " " + $sg.SignerCertificate.Subject) }
}
Get-WinEvent -LogName 'Microsoft-Windows-CodeIntegrity/Operational' -MaxEvents 5 -ErrorAction SilentlyContinue | ForEach-Object { $m = $_.Message -replace '\s+', ' '; Log ("CI " + $_.Id + " " + $m.Substring(0, [Math]::Min(260, $m.Length))) }
Log ("Politique CI : " + ((Get-ItemProperty 'HKLM:\SYSTEM\CurrentControlSet\Control\CI\Policy' -ErrorAction SilentlyContinue).VerifiedAndReputablePolicyState))
Log "DONE"
