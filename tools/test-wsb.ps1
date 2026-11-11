# Teste build/dist/JavaQuest.zip dans Windows Sandbox (Windows vierge, sans Java/Node, réseau désactivé).
# Usage : pwsh -File tools/test-wsb.ps1   (prérequis : bash tools/assemble.sh --zip)
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
  <LogonCommand><Command>powershell.exe -NoProfile -ExecutionPolicy Bypass -File C:\jq-in\inner.ps1</Command></LogonCommand>
</Configuration>
"@
$started = wsb start --config $config --raw | ConvertFrom-Json
$id = $started.Id
Write-Output "Sandbox $id démarrée"
$result = Join-Path $out "result.txt"
for ($i = 0; $i -lt 360; $i++) {
  Start-Sleep -Seconds 5
  if ((Test-Path $result) -and ((Get-Content $result -Raw -Encoding UTF8) -match "DONE")) { break }
}
Get-Content $result -Encoding UTF8
wsb stop --id $id | Out-Null
