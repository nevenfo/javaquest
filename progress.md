# PROGRESS

## Phase actuelle
Phase B terminée (B1–B4). Phase C — tranche verticale (C1) + D1 assets.

## Tâche actuelle
C1.1 — Moteur de jeu Phaser (carte, joueur, collisions, interactions).

## Dernière tâche validée
B4 — `JavaQuest.exe` (manifeste Win10/11, mutex, Job kill-on-close, arrêt via stdin) + `tools/assemble.sh` (runtime jlink 12 modules + CDS, 79 Mo).
Validation :
- `pwsh -File tools/test-launcher.ps1` → 10 PASS / 0 FAIL (copie dans un chemin avec espaces et accents, Java exécuté en sandbox avec le runtime jlink).
- `bash tools/test-server.sh` → 24 PASS ; `bash tools/test-sandbox.sh` → 26 PASS.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`.
- Sandbox : LPAC + `registryRead`, `DETACHED_PROCESS`, env minimal avec variables de profil (sinon CreateProcess 203).
- Protocole runner : lignes `\u0001<nonce> TYPE json` (C,O,E,W,X,T,L,D). API joueur sans import dans le paquetage anonyme (`runner/api/`, ex. `Sonde.voir`). Harnais : `Verif.verifier()` + `fr.javaquest.runner.Check`.
- Quêtes : `content/quests/<id>/quest.json` ; `harness/`, `solutions/`, `wrong/` jamais servis. `Validate` exige solutions OK, fautives KO, départ KO.
- Serveur : options `--app --web --content --runtime --sandbox --runner --data --port --watch-stdin`. Données par défaut `%LOCALAPPDATA%\JavaQuest`. Jeton dans le fragment `#t=`.
- Assets dans `.tools/dl/` (non versionné) : Kenney Tiny Town/Dungeon, Interface Sounds, fonts (CC0) ; Phaser 3.90.0, CodeMirror 5.65.21 (MIT) ; JetBrains Mono, Silkscreen (OFL).
- Supports ESIEE : `curriculum/originals/` (cours « Java EISC4 2026 », Java 25), jamais versionnés ni distribués.

## Blocage actif
Aucun.

## Fichiers / zones utiles
`launcher/Sandbox/` · `runner/` · `server/src/fr/javaquest/` · `content/quests/` · `tools/{build,test-server,test-sandbox}.sh` · `tests/` · `docs/` · `.tools/jdk-25` · `.tools/dl/`

## NEXT ACTION
C1.1 — Vendoriser Phaser/CodeMirror/assets dans `web/vendor` et `web/assets` (+ `THIRD_PARTY.md`), puis écrire la scène village Phaser (carte tuilée Kenney, joueur ZQSD/flèches via `event.code`, collisions, PNJ, dialogue) ; valider par capture d'écran headless Edge.
