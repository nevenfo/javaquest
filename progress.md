# PROGRESS

## Phase actuelle
Phases A, B, C1, D1, E1, E2, R1, G1 validées. En cours : F1 (contenu et traversée des régions) ; D2 et H1 ouverts.

## Tâche actuelle
F1.6 — E2E de traversée des régions.

## Dernière tâche validée
F1 contenu : 123 quêtes (lots régions + comblement des parcours), parcours complet 41/41 compétences ; champ `args` dans l'atelier ; correctif d'un SyntaxError dans `web/js/ui/editor.js` (apostrophes droites, présent depuis 540dade) et du faux positif de `waitFor` dans `tools/e2e.mjs`.
Validation :
- `bash tools/build.sh` OK ; `bash tools/test-server.sh` → 24 PASS + 123 quêtes valides, 0 échec.
- `node tools/e2e.mjs` → 24/24 (deux passages) ; `node --test tests/web/pedagogy.test.mjs` → 11/11 ; `node tools/coverage.mjs` → 41/41.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
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
F1.6 — Écrire `tools/e2e-traversee.mjs` (profil qui résout via l'API une quête par région, du village à la citadelle, avec les solutions de référence, et vérifie déblocages/portes) puis l'exécuter au vert.
