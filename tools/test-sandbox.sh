#!/usr/bin/env bash
# Vérifie réellement l'isolation de jq-sandbox avec un programme Java hostile.
# Usage : bash tools/test-sandbox.sh [runtime_dir]   (défaut : .tools/jdk-25)
set -u
cd "$(dirname "$0")/.."
ROOT="$(pwd -W 2>/dev/null || pwd)"
RT="${1:-.tools/jdk-25}"
# SB_CMD : commande du broker (défaut : exe C#) ; ex. SB_CMD=".tools/jdk-25/bin/java.exe --enable-native-access=ALL-UNNAMED -jar build/sandbox/sandbox.jar"
read -ra SB <<< "${SB_CMD:-launcher/Sandbox/bin/Release/jq-sandbox.exe}"
OUT="build/sandbox-test"
mkdir -p "$OUT"
"$RT/bin/javac" -d "$OUT" tests/sandbox/Attacks.java || exit 1
"${SB[@]}" grant "$RT" >/dev/null || { echo "grant runtime failed"; exit 1; }
"${SB[@]}" grant "$OUT" >/dev/null || { echo "grant classes failed"; exit 1; }

# Écouteur loopback de l'hôte pour vérifier que la sandbox ne peut pas joindre le serveur local.
PORT=$((20000 + RANDOM % 20000))
python -c "import socket,time;s=socket.socket();s.bind(('127.0.0.1',$PORT));s.listen();time.sleep(120)" &
LPID=$!
sleep 1

UP="${USERPROFILE//\\//}"
pass=0; fail=0
JFLAGS=(-XX:-UsePerfData -Xms8m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k)

check() { # name expect(json regex) mode arg [xmx]
  local name="$1" expect="$2" mode="$3" arg="${4:-}" xmx="${5:-128m}"
  local res
  res=$(echo "" | "${SB[@]}" run --lpac --cap registryRead --timeout-ms 6000 --cpu-ms 4000 --mem-mb 512 --max-out 65536 -- \
        "$RT/bin/java.exe" "${JFLAGS[@]}" -Xmx$xmx -cp "$ROOT/$OUT" Attacks "$mode" "$arg" 2>&1)
  if echo "$res" | grep -Eq "$expect"; then
    pass=$((pass+1)); printf 'PASS  %-34s %s\n' "$name" "$(echo "$res" | grep -Eo '"(timedOut|memoryLimit|cpuLimit|outputLimit)":true|(BLOCKED|ALLOWED)[^\\"]{0,70}' | head -2 | tr '\n' ' ')"
  else
    fail=$((fail+1)); printf 'FAIL  %-34s %s\n' "$name" "$(echo "$res" | head -c 400)"
  fi
}

check "programme légitime"              'ALLOWED hello'             ok
check "lecture fichier projet"          'BLOCKED'                   read  "$ROOT/plan.md"
check "lecture profil (NTUSER/.gitconfig)" 'BLOCKED'                read  "$UP/.gitconfig"
check "liste dossier utilisateur"        'BLOCKED|ALLOWED null'      list  "$UP"
check "liste Documents"                  'BLOCKED|ALLOWED null'      list  "$UP/Documents"
check "liste Bureau"                     'BLOCKED|ALLOWED null'      list  "$UP/Desktop"
check "écriture profil"                  'BLOCKED'                   write "$UP/jq_pwn.txt"
check "écriture C:/Users/Public"         'BLOCKED'                   write "C:/Users/Public/jq_pwn.txt"
check "écriture runtime"                 'BLOCKED'                   write "$ROOT/$RT/jq_pwn.txt"
check "écriture classes"                 'BLOCKED'                   write "$ROOT/$OUT/jq_pwn.txt"
check "réseau externe 1.1.1.1:443"       'BLOCKED'                   net   "1.1.1.1:443"
check "réseau loopback hôte"             'BLOCKED'                   net   "127.0.0.1:$PORT"
check "résolution DNS"                   'BLOCKED'                   dns   "example.com"
# Le bind d'un port est permis, mais sans capacité réseau aucune connexion n'entre ni ne sort (pare-feu AppContainer).
check "écoute TCP (bind seulement)"      'ALLOWED listening|BLOCKED' listen
# Limite documentée (docs/SECURITY.md) : les fichiers système publics restent lisibles (non personnels).
check "info: lecture système publique"   'ALLOWED|BLOCKED'           read  "C:/Windows/win.ini"
check "liste racine C: (LPAC)"           'BLOCKED|ALLOWED null'      list  "C:/"
check "info: liste Program Files"        'ALLOWED|BLOCKED'           list  "C:/Program Files"
check "création de processus"            'BLOCKED'                   proc
check "registre HKCU (Preferences)"      'BLOCKED|"exitCode":[^0]'   prefs
check "environnement minimal"            'ALLOWED \[[A-Za-z_, ]*\]'  env
check "boucle infinie"                   '"(timedOut|cpuLimit)":true' loop
check "processus persistant"             '"timedOut":true'           persist
check "bombe mémoire (Xmx 4g)"           '"memoryLimit":true|OutOfMemory|"exitCode":[^0]' mem "" 4g
check "bombe de threads"                 'threads started|"timedOut":true|"memoryLimit":true|OutOfMemory' threads
check "flood de sortie"                  '"outputLimit":true'        flood

kill $LPID 2>/dev/null
rm -f "$UP/jq_pwn.txt" "C:/Users/Public/jq_pwn.txt" 2>/dev/null
# Aucun processus résiduel : le Job (kill-on-close) doit avoir tout terminé.
left=$(tasklist //FI "IMAGENAME eq java.exe" //V 2>/dev/null | grep -ci "attacks" || true)
if [ "$left" = "0" ]; then pass=$((pass+1)); echo "PASS  aucun processus Attacks résiduel"; else fail=$((fail+1)); echo "FAIL  processus résiduels : $left"; fi
echo "RÉSULTAT : $pass PASS, $fail FAIL"
[ "$fail" = 0 ]
