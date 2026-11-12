# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, G1 validées. En cours : F1 (F1.1–F1.3, F1.6, F1.7 faits) ; D2 et H1 ouverts.

## Tâche actuelle
F1.4–F1.5 — donjon de la région 4 (collections), région finale (compagnon programmable).

## Dernière tâche validée
F1.3 : donjon région 3 « Labyrinthe des Lignées » (carte `labyrinthe`, PNJ `heraut`/`sphinx`, quêtes `d3-01`–`d3-04` après `t09-contrats`, flag `f-labyrinthe`).
Validation :
- `bash tools/build.sh` OK ; `bash tools/test-server.sh` → 135 quêtes valides, 0 échec.
- `node --test tests/web/pedagogy.test.mjs tests/web/maps.test.mjs` → 22/22 ; `node tools/e2e.mjs` → 24/24 ; `node tools/e2e-traversee.mjs` → 99 PASS, 0 FAIL ; `node tools/coverage.mjs` → 41/41.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
- Sprites de PNJ : uniquement des index de personnages déjà nommés dans `SPRITES` (101–107 sont des objets) ; libres : slime 108, bat 120, rat 123.
- Toute nouvelle carte doit passer `tests/web/maps.test.mjs` (le vide est solide ; relier les salles par `carve`).
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
F1.4 — Concevoir puis implémenter le donjon de la région 4 (tableaux/collections, accès depuis la Grande Bibliothèque après `f-bibliotheque` ou la dernière quête principale de Lyra) sur le modèle des donjons existants : carte, PNJ, quêtes `d4-01`–`d4-04` ; valider par `bash tools/test-server.sh`, `node --test tests/web/maps.test.mjs`, `node tools/e2e.mjs`, `node tools/e2e-traversee.mjs`, `node tools/coverage.mjs`.
