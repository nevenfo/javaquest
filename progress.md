# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, G1 validées. En cours : F1 (F1.1–F1.4, F1.6, F1.7 faits) ; D2 et H1 ouverts.

## Tâche actuelle
F1.5 — région finale (projet intégrateur, compagnon programmable).

## Dernière tâche validée
F1.4 : donjon région 4 « Archives Englouties » (carte `archives`, PNJ `greffier`/`pipistrelle`, quêtes `d4-01`–`d4-04` après `b07-classement-livres`, flag `f-archives`) ; `b07-classement-livres` pose enfin `f-bibliotheque`.
Validation :
- `bash tools/build.sh` OK ; `bash tools/test-server.sh` → 139 quêtes valides, 0 échec.
- `node --test tests/web/pedagogy.test.mjs tests/web/maps.test.mjs` → 23/23 ; `node tools/e2e.mjs` → 24/24 ; `node tools/e2e-traversee.mjs` → 107 PASS, 0 FAIL ; `node tools/coverage.mjs` → 41/41.

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
F1.5 — Concevoir la région finale (projet intégrateur avec compagnon programmable : le code du joueur pilote un acteur du monde via l'API de scène de `docs/CONTENT.md` § « Rendre le code visible dans le monde »), préciser ses tâches dans `plan.md`, puis l'implémenter ; valider par `bash tools/test-server.sh`, `node --test tests/web/maps.test.mjs`, `node tools/e2e.mjs`, `node tools/e2e-traversee.mjs`.
