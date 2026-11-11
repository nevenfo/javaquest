#!/usr/bin/env bash
# Compile runner (sandbox) et serveur. Usage : bash tools/build.sh [jdk_dir]
set -euo pipefail
cd "$(dirname "$0")/.."
JDK="${1:-.tools/jdk-25}"
rm -rf build/runner build/server
mkdir -p build/runner/classes build/server/classes
"$JDK/bin/javac" -encoding UTF-8 -g -d build/runner/classes \
  server/src/fr/javaquest/common/Json.java $(find runner/src -name '*.java') runner/api/*.java
"$JDK/bin/jar" --create --file build/runner/runner.jar -C build/runner/classes .
"$JDK/bin/javac" -encoding UTF-8 -g -d build/server/classes $(find server/src -name '*.java')
"$JDK/bin/jar" --create --file build/server/server.jar --main-class fr.javaquest.server.Main -C build/server/classes .
echo "build OK"
