#!/usr/bin/env bash
# Assemble la distribution dans build/dist/JavaQuest (runtime jlink, serveur, runner, sandbox, web, contenu).
# Usage : bash tools/assemble.sh [--zip] [--no-build]
set -euo pipefail
cd "$(dirname "$0")/.."
JDK=.tools/jdk-25
DIST=build/dist/JavaQuest
APP=$DIST/app
ZIP=0; NOBUILD=0
for a in "$@"; do [ "$a" = "--zip" ] && ZIP=1; [ "$a" = "--no-build" ] && NOBUILD=1; done
if [ $NOBUILD = 0 ]; then
  bash tools/build.sh >/dev/null
  dotnet build launcher/Sandbox -c Release -nologo -v q >/dev/null
fi
dotnet build launcher/Launcher -c Release -nologo -v q >/dev/null
rm -rf "$DIST"
mkdir -p "$APP/bin" "$APP/runner"
# Runtime minimal : serveur (httpserver), compilateur (jdk.compiler), API usuelles du cours.
"$JDK/bin/jlink" --module-path "$JDK/jmods" \
  --add-modules java.base,java.compiler,jdk.compiler,jdk.httpserver,java.logging,java.management,java.prefs,jdk.localedata,jdk.zipfs,jdk.crypto.ec \
  --include-locales=fr,en --strip-debug --no-header-files --no-man-pages --compress=zip-6 \
  --generate-cds-archive --output "$APP/runtime"
cp build/server/server.jar "$APP/server.jar"
cp build/runner/runner.jar "$APP/runner/runner.jar"
cp launcher/Sandbox/bin/Release/jq-sandbox.exe launcher/Sandbox/bin/Release/jq-sandbox.exe.config "$APP/bin/"
cp launcher/Launcher/bin/Release/JavaQuest.exe launcher/Launcher/bin/Release/JavaQuest.exe.config "$DIST/"
cp -r web "$APP/web"
cp -r content "$APP/content"
[ -f THIRD_PARTY.md ] && cp THIRD_PARTY.md "$DIST/"
[ -d licenses ] && cp -r licenses "$DIST/licenses"
[ -f docs/LISEZMOI.txt ] && cp docs/LISEZMOI.txt "$DIST/LISEZMOI.txt"
du -sh "$DIST" | cut -f1 | sed 's/^/taille : /'
if [ $ZIP = 1 ]; then
  rm -f build/dist/JavaQuest.zip
  (cd build/dist && powershell -NoProfile -Command "Compress-Archive -Path 'JavaQuest' -DestinationPath 'JavaQuest.zip' -CompressionLevel Optimal")
  ls -la build/dist/JavaQuest.zip
fi
echo "assemble OK"
