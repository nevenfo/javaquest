# Audit de Java Quest 0.3 (2026-11-11)

Version auditée : tag Git `legacy-v0.3` (copie aussi dans `..\javaquest-backups\2026-11-11-avant-refonte`). Machine : <machine>.

## Constat
- **Stack** : serveur Node.js (`server.mjs`, ~150 lignes minifiées), front HTML/JS natif (`public/app.js` 45 Ko, `catalog.js` 40 Ko), JDK Temurin 25 portable dans `.tools/jdk-25`. Pas de Git, pas de build.
- **Exécution Java** : `javac`/`java` lancés avec les droits complets de la session Windows ; timeout + taille de sortie seulement. **Aucune isolation** (reconnu dans le README).
- **Contenu** : 9 « mondes », 37 activités dont 17 exercices de code ; graders codés en dur dans `server.mjs` ; 65 exigences inventoriées (`curriculum/coverage.json`), couverture prouvée : 0.
- **Progression** : XP, `localStorage` + `data/progress.json` (+ `.bak`), critère de maîtrise conservateur (`mastery.js`).
- **Tests** : 21 tests Node (contrats de données, chemins, quelques exercices).
- **Distribution** : nécessite Node.js installé ; lanceurs `.cmd`/`.ps1`.
- **Sources ESIEE** : 9 documents texte du cours « Java EISC4 2026 » copiés dans `curriculum/originals/` (lecteur Y: d'origine non monté le 11/11). Le dossier OneDrive « Algorithmique et programmation » concerne le C, pas Java.
- **Licences** : aucun asset tiers ; code maison.

## Décisions
| Composant | Décision | Justification |
|---|---|---|
| Serveur Node `server.mjs` | **Remplacer** (serveur Java 25 `jdk.httpserver`) | Embarquer Node ajouterait un 2ᵉ runtime ; le JDK est déjà requis. Code minifié difficile à maintenir. Les bonnes pratiques (loopback, en-têtes CSP, limites de taille, chemins validés) sont **reprises**. |
| Exécution `javac`/`java` directe | **Remplacer** | Non isolée. Nouveau : compilation en mémoire + exécution dans AppContainer LPAC + Job Object (`jq-sandbox.exe`), vérifiée par `tools/test-sandbox.sh`. |
| Interface `public/*` (quiz/cartes) | **Remplacer** | Ce n'est pas un jeu ; nouveau front Phaser 3 + CodeMirror 5. Les acquis d'ergonomie (raccourcis Ctrl+Entrée, éditeur visible à 1366×768, onglets multi-fichiers) sont **conservés** comme exigences. |
| `catalog.js` (37 activités) | **Améliorer** (matière première) | Les énoncés originaux et idées d'exercices sont réécrits en quêtes, avec solutions de référence et tests vérifiés automatiquement. |
| `coverage.json` / `MATRICE.md` | **Améliorer** | Deviennent `content/skills.json` (compétences, prérequis, provenance) validé par script. |
| `mastery.js` | **Améliorer** | Principe conservé (aide ≠ maîtrise, preuves distinctes, différé) ; intégré au nouveau modèle de preuves + réactivations (docs/PEDAGOGIE.md). |
| Sauvegarde `data/progress.json` | **Améliorer** | Nouveau stockage par profil dans `%LOCALAPPDATA%\JavaQuest`, écriture atomique + `.bak` + version de schéma. Ancien fichier conservé, importable. |
| Tests Node | **Remplacer** | Tests Java (serveur/runner), tests de sandbox, tests front (node --test, outil de dev uniquement), E2E navigateur. |
| Lanceurs `.cmd`/`.ps1` | **Remplacer** | `JavaQuest.exe` (C# .NET Framework 4.8, présent sur Windows 10/11). |
| JDK Temurin 25 | **Conserver** | Version exigée par le cours (IO.println, `void main`, records). Embarqué via `jlink`. |
| Copies des supports ESIEE | **Conserver localement, hors Git et hors ZIP** | Documents protégés : consultation privée uniquement. |

## Rollback
`git checkout legacy-v0.3` restaure l'ancienne application (exige Node ≥ 20) ; la copie de sauvegarde externe contient aussi les données.
