# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, R1 validées. En cours : F1 (contenu des régions) en parallèle de G1 (examen) et E2/D2.

## Tâche actuelle
F1 — production du contenu des régions (spécification `docs/CONTENT.md`).

## Dernière tâche validée
C1 tranche verticale : village Phaser jouable, atelier CodeMirror, quêtes q01–q04 (guidée, prédiction, complétion, autonome à entrées variées), sauvegarde/rechargement.
Validation :
- `node tools/e2e.mjs` → 19/19 sur 4 passages consécutifs (1 échec isolé non reproduit, non identifié : à surveiller).
- `bash tools/test-server.sh` → 24 PASS + contenu 4/4 ; `bash tools/test-sandbox.sh` → 26 PASS ; `pwsh -File tools/test-launcher.ps1` → 10 PASS.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md` (règles P1–P19, hypothèses H1–H5).
- Front : Phaser 3.90 (scène `web/js/world/scene.js`, cartes `maps.js` via `builder.js`), UI DOM (`web/js/ui/*`), modèle pédagogique pur `web/js/pedagogy.js`, sauvegarde `state.js` (throttle 1 s + keepalive).
- Runner : stdin servi ligne par ligne avec écho terminal (flux `in`) ; `System.in` installé une seule fois (IO du JDK le met en cache).
- Quêtes : formats code/fix/parsons/predict/quiz ; clé de quiz et harnais jamais envoyés au client ; `Validate` vérifie solutions, fautives, départ, prédictions.
- E2E : Edge headless + CDP, émulation de focus obligatoire (sinon Phaser s'endort).
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun.

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (49,8 Ko, non versionné, absent de la sauvegarde) a été supprimé par erreur le 2026-11-11 lors du nettoyage des fichiers hérités. Archive équivalente reconstituée depuis le tag : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip` (contenu identique au dépôt hérité, pas forcément au ZIP d'origine).

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` · `runner/` · `launcher/` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e.mjs,test-launcher.ps1}` · `docs/`

## NEXT ACTION
F1 — Intégrer les lots de contenu des deux producteurs (régions village/forêt/mines puis artisans/tour/bibliothèque/citadelle) : `bash tools/test-server.sh` tout OK, `node tools/coverage.mjs` (viser parcours complet par compétence), revue d'échantillon, puis commit ; ensuite H1.2 (test Windows Sandbox du ZIP).
