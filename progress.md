# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, F1, G1 validées. Ouverts : D2 (tests unitaires état/sauvegarde manquants) puis H1 (distribution).

## Tâche actuelle
D2 — tests unitaires état/sauvegarde (`web/js/state.js`).

## Dernière tâche validée
F1.5 : région finale « Observatoire du Sommet » (carte `sommet`, PNJ `astronome`, portail village nord `requires: 'f-bibliotheque'`), quêtes `s01`–`s04` pilotant le compagnon (`support/Compagnon.java`, identique sauf `GRILLE_ESSAI`), `s04` pose `f-sommet` (conclusion du jeu).
Validation :
- `bash tools/build.sh` OK ; `bash tools/test-server.sh` → 143 quêtes valides, 0 échec.
- `node --test tests/web/pedagogy.test.mjs tests/web/maps.test.mjs` → 24/24 ; `node tools/e2e.mjs` → 24/24 ; `node tools/e2e-traversee.mjs` → 124 PASS, 0 FAIL ; `node tools/coverage.mjs` → 143 activités, 41/41.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Région finale `sommet` : API du compagnon dans `docs/CONTENT.md` § « Le compagnon de l'Observatoire ».
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
D2 — Écrire `tests/web/state.test.mjs` (node --test) couvrant création/sélection de profils, sauvegarde/chargement et migration d'état de `web/js/state.js` (stockage simulé) ; si vert avec l'E2E menus existant (`node tools/e2e.mjs`), cocher D2.1–D2.3 avec leur preuve.
