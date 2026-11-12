# PROGRESS

## Phase actuelle
Phases A–G validées. Reste : H1 (H1.1 fait ; H1.2 partiel ; H1.3 en attente de décision).

## Tâche actuelle
H1.3 — rendre le lancement compatible avec Smart App Control.

## Dernière tâche validée
H1.3.1 : broker d'isolation porté en Java FFM (`sandbox/src/fr/javaquest/sandbox/{Main,Win}.java` → `build/sandbox/sandbox.jar`, même CLI que `jq-sandbox.exe`) ; `tools/test-sandbox.sh` accepte `SB_CMD`.
Validation :
- `SB_CMD=".tools/jdk-25/bin/java.exe -XX:+UseSerialGC -Xmx64m -XX:TieredStopAtLevel=1 -XX:-UsePerfData --enable-native-access=ALL-UNNAMED -jar build/sandbox/sandbox.jar" bash tools/test-sandbox.sh` → 26 PASS, 0 FAIL.
- Windows Sandbox (Smart App Control actif), session utilisateur : broker accepté, enfant LPAC OK, réseau / profil / processus bloqués.
- Antérieur : `tools/test-dist.ps1` 15/15 ; `npm run -s test:web` 33/33 ; `node tools/e2e.mjs` 33/33.

## Décisions actives
- Windows Sandbox : exécuter les scénarios en session utilisateur (`wsb connect` puis `wsb exec -r ExistingLogin`) ; en contexte `System` (session 0), tout enfant AppContainer échoue en `0xC0000142`. Sous PowerShell 5, quoter les options `-XX:...` passées à un exe.
- H1.3 (décision utilisateur 2026-11-12) : distribution gratuite, portable, hors ligne, Win10/11 avec Smart App Control actif, sans certificat payant, sans admin. Voie : remplacer `JavaQuest.exe` et `jq-sandbox.exe` par du Java FFM exécuté par le `java(w).exe` signé, **isolation AppContainer LPAC + Job Object intégralement préservée**. Prototype minimal validé avant refonte ; si l'équivalence de sécurité n'est pas démontrée (`tools/test-sandbox.sh` 26/26 + WSB 12/12), garder l'architecture actuelle et présenter signature / MSIX Store.
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Région finale `sommet` : API du compagnon dans `docs/CONTENT.md` § « Le compagnon de l'Observatoire ».
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
- Sprites de PNJ : uniquement des index de personnages déjà nommés dans `SPRITES` (101–107 sont des objets) ; libres : slime 108, bat 120, rat 123.
- Toute nouvelle carte doit passer `tests/web/maps.test.mjs` (le vide est solide ; relier les salles par `carve`).
- E2E : Edge headless + CDP, émulation de focus obligatoire ; `waitFor` échoue si l'évaluation lève une exception.
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun (décision H1.3 prise).

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` · `runner/` · `launcher/` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e.mjs,coverage.mjs,test-launcher.ps1}` · `docs/`

## NEXT ACTION
H1.3.2 — Faire lancer le broker Java par `SandboxRunner`/`Main` (option `sandbox` → commande `runtime/bin/java.exe … -jar app/bin/sandbox.jar`, `grant` compris) à la place de `jq-sandbox.exe` ; valider par `bash tools/test-server.sh` (143), `node tools/e2e.mjs` et `node tools/e2e-traversee.mjs`.
