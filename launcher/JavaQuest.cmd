@echo off
rem JavaQuest : lance le serveur local dans le javaw.exe signe du runtime embarque (aucun exe non signe).
rem Options (transmises) : --no-browser, --idle-seconds <n>. Journal : %LOCALAPPDATA%\JavaQuest\javaquest.log
setlocal
set "JQ_APP=%~dp0app"
if not exist "%JQ_APP%\runtime\bin\javaw.exe" goto missing
if not exist "%JQ_APP%\server.jar" goto missing
start "" /D "%JQ_APP%" "%JQ_APP%\runtime\bin\javaw.exe" -Xmx256m -XX:+UseSerialGC -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "%JQ_APP%\server.jar" fr.javaquest.server.Launch --app "%JQ_APP%" %*
exit /b 0
:missing
echo Fichiers du jeu introuvables. Extrais TOUT le contenu du ZIP dans un dossier, puis relance JavaQuest.cmd.
pause
exit /b 1
