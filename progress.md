# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, G1 validées. En cours : F1 (F1.1, F1.2, F1.6, F1.7 faits) ; D2 et H1 ouverts.

## Tâche actuelle
F1.3–F1.5 — donjons des régions 3 et 4, région finale (compagnon programmable).

## Dernière tâche validée
F1.7 : `tests/web/maps.test.mjs` (BFS depuis chaque spawn : PNJ, objets, portails, arrivées `at`). Correctifs : `MapBuilder` rend la base `void` infranchissable (seuls `room`/`carve` libèrent) ; couloirs ajoutés dans `mines()` (spectre) et `tour()` (mage).
Validation :
- `node --test tests/web/pedagogy.test.mjs tests/web/maps.test.mjs` → 21/21 ; mutation (brèche de la crypte rebouchée) → échec « PNJ golem atteignable », comme attendu.
- `bash tools/build.sh` OK ; `node tools/e2e.mjs` → 24/24 ; `node tools/e2e-traversee.mjs` → 92 PASS, 0 FAIL ; `bash tools/test-server.sh` → 131 quêtes valides (commit précédent, contenu inchangé).

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
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
F1.3 — Concevoir puis implémenter le donjon de la région 3 (interfaces/hiérarchies, accès depuis la Tour des Contrats après `f-tour`) sur le modèle de la crypte et de la fonderie : carte, PNJ, quêtes `d3-01`–`d3-04` ; valider par `bash tools/test-server.sh`, `node --test tests/web/maps.test.mjs`, `node tools/e2e.mjs`, `node tools/e2e-traversee.mjs`, `node tools/coverage.mjs`.
