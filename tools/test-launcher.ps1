# Teste JavaQuest.cmd depuis une copie de la distribution (chemin avec espaces) :
# démarrage, santé de l'API, exécution Java isolée, double lancement, arrêt par /api/quit, arrêt brutal (kill du serveur),
# arrêt automatique sur inactivité, absence de processus résiduel.
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
function Launch($extra = "--no-browser") {
  # cmd /c avec guillemets externes : seule forme fiable pour un .cmd dans un chemin avec espaces
  Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"`"$cmd`" $extra`"" -PassThru -WindowStyle Hidden
}
function WaitSession($old = $null) {
  for ($i = 0; $i -lt 80; $i++) {
    Start-Sleep -Milliseconds 500
    if (Test-Path $session) { $u = (Get-Content $session -Raw).Trim(); if ($u -and $u -ne $old) { return $u } }
  }
  return $null
}
function WaitNoServer($sec) {
  for ($i = 0; $i -lt $sec * 2; $i++) { if (@(ServerProcs $Target).Count -eq 0) { return $true }; Start-Sleep -Milliseconds 500 }
  return $false
}
function ServerProcs($dir) {
  Get-CimInstance Win32_Process -Filter "name='javaw.exe' or name='java.exe'" | Where-Object { $_.CommandLine -like "*$dir*" }
}

if (Test-Path $Target) { Remove-Item -Recurse -Force $Target }
New-Item -ItemType Directory -Force (Split-Path $Target) | Out-Null
Copy-Item -Recurse $Source $Target
$cmd = Join-Path $Target "JavaQuest.cmd"
$appDir = Join-Path $Target "app"
$key = [BitConverter]::ToString([Security.Cryptography.SHA256]::Create().ComputeHash([Text.Encoding]::UTF8.GetBytes($appDir.ToLowerInvariant())), 0, 6).Replace("-", "").ToLowerInvariant()
$session = Join-Path $env:LOCALAPPDATA "JavaQuest\session-$key.txt"

# 1. Démarrage
$p = Launch
$url = WaitSession
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
  $c = Invoke-RestMethod "$base/api/content" -Headers @{ "X-JQ-Token" = $token }
  Check "contenu chargé (quêtes)" ($c.quests.Count -ge 100) "quêtes=$($c.quests.Count)"
  $ok = '{"quest":"q01-porte","files":[{"name":"Main.java","content":"void main() { IO.println(\"Ouvre-toi !\"); }"}]}'
  $r = Invoke-RestMethod "$base/api/check" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($ok)) -Headers @{ "X-JQ-Token" = $token }
  Check "quête vérifiée par son harnais : solution acceptée" ($r.passed -eq $true) ($r | ConvertTo-Json -Compress -Depth 5)
  $ko = '{"quest":"q01-porte","files":[{"name":"Main.java","content":"void main() { IO.println(\"Ouvre-toi\"); }"}]}'
  $r = Invoke-RestMethod "$base/api/check" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($ko)) -Headers @{ "X-JQ-Token" = $token }
  Check "quête vérifiée par son harnais : code fautif refusé" ($r.passed -eq $false) ($r | ConvertTo-Json -Compress -Depth 5)

  # 2. Double lancement : la 2e instance doit se terminer d'elle-même sans 2e serveur
  $p2 = Launch
  $exited = $p2.WaitForExit(10000)
  Start-Sleep -Seconds 2
  Check "double lancement : 2e instance se termine" $exited
  $count = @(ServerProcs $Target).Count
  Check "un seul serveur actif" ($count -eq 1) "serveurs=$count"

  $same = (Get-Content $session -Raw).Trim() -eq $url
  Check "double lancement : même session conservée" $same

  # 3. Arrêt par /api/quit (jeton exigé)
  $code = 0
  try { Invoke-WebRequest "$base/api/quit" -Method Post -Body "{}" -UseBasicParsing | Out-Null } catch { $code = [int]$_.Exception.Response.StatusCode }
  Check "/api/quit sans jeton refusé" ($code -eq 403) "code=$code"
  Check "serveur toujours actif après un quit refusé" (@(ServerProcs $Target).Count -eq 1)
  $q = Invoke-RestMethod "$base/api/quit" -Method Post -Body "{}" -Headers @{ "X-JQ-Token" = $token }
  Check "/api/quit accepté avec jeton" ($q.ok -eq $true)
  Check "arrêt par /api/quit : aucun serveur résiduel" (WaitNoServer 15)
  Check "fichier de session supprimé" (-not (Test-Path $session))
}

# 4. Arrêt brutal : kill du javaw serveur, puis relancement (verrou libéré)
$url1 = $null
$p = Launch; $url1 = WaitSession
Check "relancement après arrêt propre" ($url1 -ne $null)
ServerProcs $Target | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
Check "arrêt brutal du serveur : plus aucun processus java/javaw du dossier" (WaitNoServer 10)
$p = Launch; $url2 = WaitSession $url1
Check "relancement après arrêt brutal (verrou libéré, session périmée remplacée)" ($url2 -ne $null -and $url2 -ne $url1)
if ($url2) {
  $b2 = $url2.Substring(0, $url2.IndexOf("/#t=")); $t2 = $url2.Substring($url2.IndexOf("#t=") + 3)
  $h = Invoke-RestMethod "$b2/api/health" -Headers @{ "X-JQ-Token" = $t2 }
  Check "serveur relancé fonctionnel" ($h.ok -eq $true)
  Invoke-RestMethod "$b2/api/quit" -Method Post -Body "{}" -Headers @{ "X-JQ-Token" = $t2 } | Out-Null
  Check "arrêt après relancement" (WaitNoServer 15)
}

# 5. Arrêt automatique sur inactivité (délai court passé en option)
$p = Launch "--no-browser --idle-seconds 8"
$url3 = WaitSession
Check "démarrage avec --idle-seconds 8" ($url3 -ne $null)
Check "arrêt automatique sur inactivité" (WaitNoServer 40)
Check "session supprimée après arrêt automatique" (-not (Test-Path $session))
if (Test-Path $session) { Remove-Item $session }

Write-Output "RÉSULTAT : $pass PASS, $fail FAIL"
if ($fail -gt 0) { exit 1 }
