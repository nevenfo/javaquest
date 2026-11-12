# Teste build/dist/JavaQuest.zip dans Windows Sandbox (Windows vierge, sans Java/Node, réseau désactivé).
# Usage : pwsh -File tools/test-wsb.ps1   (prérequis : bash tools/assemble.sh --zip ; sortie : build/wsb/out/result.txt)
$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$in = Join-Path $root "build\wsb\in"; $out = Join-Path $root "build\wsb\out"
Remove-Item -Recurse -Force (Join-Path $root "build\wsb") -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $in, $out | Out-Null
Copy-Item (Join-Path $root "build\dist\JavaQuest.zip") $in
# PowerShell 5 (dans la sandbox) exige un BOM pour lire les accents du script.
$src = Get-Content (Join-Path $PSScriptRoot "wsb\inner.ps1") -Raw -Encoding UTF8
[IO.File]::WriteAllText((Join-Path $in "inner.ps1"), $src, (New-Object Text.UTF8Encoding($true)))
$config = @"
<Configuration>
  <Networking>Disable</Networking>
  <MappedFolders>
    <MappedFolder><HostFolder>$in</HostFolder><SandboxFolder>C:\jq-in</SandboxFolder><ReadOnly>true</ReadOnly></MappedFolder>
    <MappedFolder><HostFolder>$out</HostFolder><SandboxFolder>C:\jq-out</SandboxFolder><ReadOnly>false</ReadOnly></MappedFolder>
  </MappedFolders>
</Configuration>
"@
$started = wsb start --config $config --raw | ConvertFrom-Json
$id = $started.Id
Write-Output "Sandbox $id démarrée"
$result = Join-Path $out "result.txt"
# La CLI `wsb` démarre la sandbox sans session utilisateur : une LogonCommand ne s'exécuterait jamais.
# On exécute donc le scénario dans le contexte système (attente du démarrage complet, puis wsb exec).
for ($i = 0; $i -lt 24; $i++) {
  wsb exec --id $id -r System -c "cmd.exe /c exit 0" 2>&1 | Out-Null
  if ($LASTEXITCODE -eq 0) { break }; Start-Sleep -Seconds 5
}
wsb exec --id $id -r System -c "powershell.exe -NoProfile -ExecutionPolicy Bypass -File C:\jq-in\inner.ps1" | Out-Null
Get-Content $result -Encoding UTF8
wsb stop --id $id | Out-Null
# Verdict : chaque attente doit apparaître dans le journal produit dans la sandbox.
$log = if (Test-Path $result) { Get-Content $result -Raw -Encoding UTF8 } else { "" }
$expect = [ordered]@{
  "pas de java dans le PATH" = "java dans le PATH : False"; "pas de node dans le PATH" = "node dans le PATH : False"
  "réseau coupé" = "Internet joignable : False"; "ZIP extrait" = "ZIP extrait : True"; "serveur démarré" = "Serveur démarré : True"
  "santé + sandbox" = "Santé : ok=True sandbox=True"; "Java exécuté" = "Java exécuté : status=ok stdout=Somme 55"
  "fichier personnel inaccessible au code" = "Lecture fichier perso depuis le code : status=(?!ok)\w+"; "quêtes chargées" = "Quêtes chargées : \d{3}"
  "page du jeu" = "Page du jeu : 200"; "arrêt propre sans processus résiduel" = "Arrêt propre : True ; processus Java restants : 0"; "aucune erreur" = "^(?![\s\S]*ERREUR)[\s\S]*DONE"
}
$bad = 0
foreach ($k in $expect.Keys) { if ($log -match $expect[$k]) { Write-Output "PASS  wsb : $k" } else { Write-Output "FAIL  wsb : $k"; $bad++ } }
Write-Output "RÉSULTAT WSB : $($expect.Count - $bad) PASS, $bad FAIL"
if ($bad -gt 0) { exit 1 }
