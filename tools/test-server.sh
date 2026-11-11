#!/usr/bin/env bash
# Tests d'intégration serveur + runner + sandbox, puis validation du contenu.
set -u
cd "$(dirname "$0")/.."
JDK=.tools/jdk-25
ARGS=(--app . --web web --content content --runtime "$JDK" --sandbox launcher/Sandbox/bin/Release/jq-sandbox.exe --runner build/runner/runner.jar --data build/testdata)
rm -rf build/testdata
"$JDK/bin/java" -Dstdout.encoding=UTF-8 -cp build/server/server.jar tests/server/ServerTest.java "${ARGS[@]}"; s1=$?
"$JDK/bin/java" -Dstdout.encoding=UTF-8 -cp build/server/server.jar fr.javaquest.server.Validate --content content --runtime "$JDK" --sandbox launcher/Sandbox/bin/Release/jq-sandbox.exe --runner build/runner/runner.jar --data build/testdata; s2=$?
[ $s1 = 0 ] && [ $s2 = 0 ]
