# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, G1 validées. En cours : F1 (donjons par région) ; D2 et H1 ouverts.

## Tâche actuelle
F1.2–F1.5 — donjons des régions 2 à 4 et région finale (compagnon programmable) : absents du code.

## Dernière tâche validée
F1.1 : donjon région 1 « Crypte de Boucle-Profonde » (carte `crypte` dans `web/js/world/maps.js`, PNJ `gardien`/`golem`, quêtes `d1-01`–`d1-04` optionnelles chaînées après `m15-coffre`, flag `f-crypte`).
Validation :
- `bash tools/build.sh` OK ; `bash tools/test-server.sh` → 127 quêtes valides, 0 échec.
- `node tools/e2e-traversee.mjs` → 85 PASS, 0 FAIL ; `node tools/e2e.mjs` → 24/24 ; `node --test tests/web/pedagogy.test.mjs` → 11/11 ; `node tools/coverage.mjs` → 41/41.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
- E2E : Edge headless + CDP, émulation de focus obligatoire ; `waitFor` échoue si l'évaluation lève une exception.
- D2 : fonctionnalités couvertes par l'E2E, mais tests unitaires état/sauvegarde absents → D2 non coché.
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun.

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` · `runner/` · `launcher/` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e.mjs,coverage.mjs,test-launcher.ps1}` · `docs/`

## NEXT ACTION
F1.2 — Concevoir puis implémenter le donjon de la région 2 (objets/records, accès depuis le quartier des Artisans après `f-forge`) sur le modèle de la crypte : carte, PNJ, quêtes `d2-01`–`d2-04` ; valider par `bash tools/test-server.sh`, `node tools/e2e.mjs`, `node tools/e2e-traversee.mjs`, `node tools/coverage.mjs`.
