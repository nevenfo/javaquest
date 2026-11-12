# JavaQuest 2.0 — Les Runes de Javelune

*Un jeu de **Nevant** — conception, contenu pédagogique et développement. © 2026 Nevant.*

RPG fantastique 2D (vue de dessus) pour apprendre **Java 25** à partir des objectifs du cours *Java EISC4* (ESIEE / Université Gustave Eiffel). Chaque sort est un vrai programme Java : compilé par `javac`, exécuté dans un conteneur Windows isolé, et rejoué dans le monde (paroles, actions des acteurs, valeurs observées, ligne par ligne).

## Pour jouer
Télécharger `JavaQuest.zip`, l'extraire, double-cliquer `JavaQuest.cmd` (Windows 10/11 64 bits, compatible Smart App Control : seuls les binaires Java signés du runtime sont exécutés). Aucun prérequis, hors ligne, sans compte. Voir `docs/LISEZMOI.txt` (copié à la racine du ZIP).

## Architecture
| Dossier | Rôle |
|---|---|
| `launcher/JavaQuest.cmd` | point d'entrée : lance `javaw.exe` (signé) sur `fr.javaquest.server.Launch` (instance unique, navigateur, arrêt par le jeu ou sur inactivité) |
| `sandbox/` | broker d'isolation `sandbox.jar` (Java FFM) : AppContainer LPAC + Job Object + politique sans processus enfant (voir `docs/SECURITY.md`) |
| `server/` | serveur local Java 25 (`jdk.httpserver`, 127.0.0.1 + jeton), sauvegardes `%LOCALAPPDATA%\JavaQuest`, notation, `Validate` |
| `runner/` | exécuté dans la sandbox : compilation en mémoire (`javax.tools`), exécution, traces, harnais `Check` |
| `web/` | jeu : Phaser 3 (monde), CodeMirror 5 (atelier), modules ES sans build |
| `content/` | `skills.json` (référentiel de compétences) et `quests/` (énoncés, harnais, solutions de référence, solutions fautives) |
| `docs/` | `AUDIT.md`, `SECURITY.md`, `PEDAGOGIE.md` (fondements scientifiques), `CONTENT.md` (écrire une quête), `COUVERTURE.md` |

## Développer
Prérequis de développement : JDK Temurin 25 dans `.tools/jdk-25`, Node ≥ 20 (tests seulement), Edge (E2E).
```
bash tools/build.sh                 # serveur, runner, broker sandbox.jar
bash tools/test-sandbox.sh          # attaques contre la sandbox
bash tools/test-server.sh           # tests HTTP + validation de tout le contenu
npm run -s test:web                 # état, sauvegarde, pédagogie, cartes
node tools/e2e.mjs                  # scénario complet dans Edge headless
bash tools/assemble.sh --zip        # build/dist/JavaQuest.zip (runtime jlink)
pwsh -File tools/test-dist.ps1 -Wsb # ZIP extrait (espaces/accents, PATH sans Java/Node), lanceur, puis Windows Sandbox (Smart App Control, réseau coupé)
```
Suivi du projet : `plan.md` (feuille de route) et `progress.md` (état courant). Version précédente (0.3) : conservée hors du dépôt public.

Les supports ESIEE (`curriculum/originals/`) servent uniquement à l'analyse locale : ils ne sont ni versionnés ni distribués.
