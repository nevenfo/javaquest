# Teste la distribution comme un étudiant : extraction de build/dist/JavaQuest.zip dans un chemin avec espaces/accents,
# PATH réduit au système (ni Java ni Node), puis tests du lanceur (tools/test-launcher.ps1) sur la copie extraite.
# Option -Wsb : rejoue aussi l'installation dans Windows Sandbox (Windows vierge, réseau coupé) via tools/test-wsb.ps1.
# Usage : pwsh -NoProfile -File tools/test-dist.ps1 [-Build] [-Wsb]   (-Build : lance d'abord bash tools/assemble.sh --zip)
param([switch]$Build, [switch]$Wsb)
$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$zip = Join-Path $root "build\dist\JavaQuest.zip"
$pass = 0; $fail = 0
function Check($name, $ok, $detail = "") {
  if ($ok) { $script:pass++; Write-Output "PASS  $name" } else { $script:fail++; Write-Output "FAIL  $name $detail" }
}

# Git Bash (et non le bash.exe de WSL dans System32).
$bash = @("$env:ProgramFiles\Git\bin\bash.exe") + @(Get-Command bash -All -ErrorAction SilentlyContinue | Where-Object { $_.Source -notlike "$env:SystemRoot\*" -and $_.Source -notlike "*\WindowsApps\*" } | ForEach-Object Source) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if ($Build) { if (-not $bash) { throw "Git Bash introuvable" }; Push-Location $root; & $bash tools/assemble.sh --zip; $rc = $LASTEXITCODE; Pop-Location; if ($rc -ne 0) { throw "assemble.sh a échoué ($rc)" } }
Check "archive présente" (Test-Path $zip) $zip
if (-not (Test-Path $zip)) { Write-Output "RÉSULTAT : $pass PASS, $fail FAIL"; exit 1 }
$mb = [math]::Round((Get-Item $zip).Length / 1MB, 1)
Check "taille de l'archive raisonnable (< 120 Mo)" ($mb -lt 120) "$mb Mo"

# Extraction comme un étudiant (dossier avec espaces et accents, hors du dépôt)
$base = Join-Path $env:TEMP "javaquest-test-dist\Mes jeux é"
if (Test-Path $base) { Remove-Item -Recurse -Force $base }
New-Item -ItemType Directory -Force $base | Out-Null
Expand-Archive -Path $zip -DestinationPath $base -Force
$dist = Join-Path $base "JavaQuest"
Check "ZIP extrait : JavaQuest.cmd à la racine" (Test-Path (Join-Path $dist "JavaQuest.cmd"))
$exes = @(Get-ChildItem -Recurse -File $dist -Filter *.exe | Where-Object { $_.FullName -notlike (Join-Path $dist "app\runtime\*") })
Check "aucun .exe hors app\runtime" ($exes.Count -eq 0) ($exes.FullName -join ", ")
foreach ($f in @("app\runtime\bin\javaw.exe", "app\server.jar", "app\runner\runner.jar", "app\bin\sandbox.jar", "app\web\index.html", "app\content\skills.json", "THIRD_PARTY.md", "LISEZMOI.txt")) {
  Check "contenu de la distribution : $f" (Test-Path (Join-Path $dist $f))
}
$leaks = @(Get-ChildItem -Recurse -File $dist | Where-Object { $_.FullName -match '\\(originals|\.git)\\|\.(pdf|env)$' })
Check "aucun support ESIEE ni fichier sensible embarqué" ($leaks.Count -eq 0) ($leaks.FullName -join ", ")

# PATH minimal : ni Java ni Node (le jeu doit tout apporter)
$fullPath = $env:PATH
$env:PATH = "$env:SystemRoot\System32;$env:SystemRoot;$env:SystemRoot\System32\WindowsPowerShell\v1.0"
Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
Check "PATH sans java" (-not (Get-Command java -ErrorAction SilentlyContinue))
Check "PATH sans node" (-not (Get-Command node -ErrorAction SilentlyContinue))

$target = Join-Path $env:TEMP "javaquest-test-dist\Lancement é\JavaQuest"
$out = & "$PSScriptRoot\test-launcher.ps1" -Source $dist -Target $target 2>&1
$out | ForEach-Object { Write-Output "  $_" }
$res = ($out | Select-String "RÉSULTAT : (\d+) PASS, (\d+) FAIL" | Select-Object -Last 1)
Check "tests du lanceur sur la copie extraite" ($res -and $res.Matches[0].Groups[2].Value -eq "0") "$res"

if ($Wsb) {
  $env:PATH = $fullPath   # wsb.exe (WindowsApps) ; la sandbox reste sans Java/Node
  if (Get-Command WindowsSandbox.exe -ErrorAction SilentlyContinue) {
    $w = & "$PSScriptRoot\test-wsb.ps1" 2>&1
    $w | ForEach-Object { Write-Output "  $_" }
    Check "Windows Sandbox (Windows vierge, réseau coupé)" ($LASTEXITCODE -eq 0) ""
  } else { Write-Output "SKIP  Windows Sandbox indisponible sur ce poste" }
}

Remove-Item -Recurse -Force (Join-Path $env:TEMP "javaquest-test-dist") -ErrorAction SilentlyContinue
Write-Output "RÉSULTAT : $pass PASS, $fail FAIL"
if ($fail -gt 0) { exit 1 }
