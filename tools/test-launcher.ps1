# Teste JavaQuest.exe depuis une copie de la distribution (chemin avec espaces) :
# démarrage, santé de l'API, exécution Java isolée, double lancement, arrêt propre, arrêt brutal, absence de processus résiduel.
# Usage : powershell -NoProfile -File tools/test-launcher.ps1 [-Source build/dist/JavaQuest] [-Target "<dossier>"]
param(
  [string]$Source = "build/dist/JavaQuest",
  [string]$Target = "$env:USERPROFILE\Documents\Etabli\Projets\javaquest-tests\Dossier avec espaces é\JavaQuest"
)
$ErrorActionPreference = "Stop"
$pass = 0; $fail = 0
function Check($name, $ok, $detail = "") {
  if ($ok) { $script:pass++; Write-Output "PASS  $name" } else { $script:fail++; Write-Output "FAIL  $name $detail" }
}
function ServerProcs($dir) {
  Get-CimInstance Win32_Process -Filter "name='javaw.exe' or name='java.exe'" | Where-Object { $_.CommandLine -like "*$dir*" }
}

if (Test-Path $Target) { Remove-Item -Recurse -Force $Target }
New-Item -ItemType Directory -Force (Split-Path $Target) | Out-Null
Copy-Item -Recurse $Source $Target
$exe = Join-Path $Target "JavaQuest.exe"
$appDir = Join-Path $Target "app"
$key = [BitConverter]::ToString([Security.Cryptography.SHA256]::Create().ComputeHash([Text.Encoding]::UTF8.GetBytes($appDir.ToLowerInvariant())), 0, 6).Replace("-", "").ToLowerInvariant()
$session = Join-Path $env:LOCALAPPDATA "JavaQuest\session-$key.txt"

# 1. Démarrage
$p = Start-Process -FilePath $exe -ArgumentList "--no-browser" -PassThru
$url = $null
for ($i = 0; $i -lt 60 -and -not $url; $i++) { Start-Sleep -Milliseconds 500; if (Test-Path $session) { $url = (Get-Content $session -Raw).Trim() } }
Check "démarrage depuis un chemin avec espaces/accents" ($url -ne $null) "pas de session"
if ($url) {
  $base = $url.Substring(0, $url.IndexOf("/#t=")); $token = $url.Substring($url.IndexOf("#t=") + 3)
  $h = Invoke-RestMethod "$base/api/health" -Headers @{ "X-JQ-Token" = $token }
  Check "API santé + sandbox prête" ($h.ok -and $h.sandbox) ($h | ConvertTo-Json -Compress)
  $body = '{"files":[{"name":"Main.java","content":"void main() { IO.println(\"Bonjour \" + (6*7)); }"}]}'
  $r = Invoke-RestMethod "$base/api/run" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($body)) -Headers @{ "X-JQ-Token" = $token }
  Check "Java réel exécuté en sandbox (runtime jlink)" ($r.stdout -eq "Bonjour 42`n") ($r | ConvertTo-Json -Compress -Depth 5)
  $idx = (Invoke-WebRequest "$base/" -UseBasicParsing).StatusCode
  Check "page du jeu servie" ($idx -eq 200)

  # 2. Double lancement : la 2e instance doit se terminer d'elle-même sans 2e serveur
  $p2 = Start-Process -FilePath $exe -ArgumentList "--no-browser" -PassThru
  $exited = $p2.WaitForExit(10000)
  Check "double lancement : 2e instance se termine" $exited
  $count = @(ServerProcs $Target).Count
  Check "un seul serveur actif" ($count -eq 1) "serveurs=$count"

  # 3. Arrêt propre (fermeture de la fenêtre)
  $p.CloseMainWindow() | Out-Null
  $stopped = $p.WaitForExit(10000)
  Start-Sleep -Milliseconds 800
  Check "arrêt propre par fermeture de la fenêtre" $stopped
  Check "aucun serveur résiduel après arrêt" (@(ServerProcs $Target).Count -eq 0)
  Check "fichier de session supprimé" (-not (Test-Path $session))
}

# 4. Arrêt brutal du lanceur : le Job doit tuer le serveur
$p = Start-Process -FilePath $exe -ArgumentList "--no-browser" -PassThru
for ($i = 0; $i -lt 60 -and -not (Test-Path $session); $i++) { Start-Sleep -Milliseconds 500 }
Stop-Process -Id $p.Id -Force
Start-Sleep -Seconds 2
Check "arrêt brutal du lanceur : serveur tué (Job kill-on-close)" (@(ServerProcs $Target).Count -eq 0)
if (Test-Path $session) { Remove-Item $session }

Write-Output "RÉSULTAT : $pass PASS, $fail FAIL"
if ($fail -gt 0) { exit 1 }
