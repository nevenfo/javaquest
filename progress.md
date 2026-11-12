# PROGRESS

## Phase actuelle
Phases A–G validées (A, B, C1, D1, D2, E1, E2, R1, F1, G1). Reste : H1 (distribution ZIP Windows).

## Tâche actuelle
H1.1 — distribution autonome (`tools/assemble.sh` existe déjà : jlink, serveur, runner, sandbox, web, contenu, `--zip`).

## Dernière tâche validée
D2 (D2.1–D2.3) : `tests/web/state.test.mjs` (état, migration, sauvegarde différée, profils) ; `Session.dispose()` arrête l'ancienne session au changement de profil (avant : chronomètre et sauvegardes continuaient) ; E2E étendu (pas-à-pas, indices gradués, journal/sac, carte, second profil).
Validation :
- `npm run -s test:web` → 33/33 (mutation « dispose sans clearInterval » → échec attendu).
- `node tools/e2e.mjs` → 33 PASS, 0 FAIL.
- F1.5 (précédent) : `tools/test-server.sh` 143 quêtes, traversée E2E 124/124.

## Décisions actives
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Région finale `sommet` : API du compagnon dans `docs/CONTENT.md` § « Le compagnon de l'Observatoire ».
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
- Sprites de PNJ : uniquement des index de personnages déjà nommés dans `SPRITES` (101–107 sont des objets) ; libres : slime 108, bat 120, rat 123.
- Toute nouvelle carte doit passer `tests/web/maps.test.mjs` (le vide est solide ; relier les salles par `carve`).
- E2E : Edge headless + CDP, émulation de focus obligatoire ; `waitFor` échoue si l'évaluation lève une exception.
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun.

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` · `runner/` · `launcher/` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e.mjs,coverage.mjs,test-launcher.ps1}` · `docs/`

## NEXT ACTION
H1.1 — Faire produire `build/dist/JavaQuest.zip` par `bash tools/assemble.sh --zip` (le plan nomme `tools/build-dist` : aligner le plan sur `assemble.sh` plutôt que dupliquer), puis écrire `tools/test-dist` (extraction dans un chemin avec espaces, PATH sans Java/Node, lancement de `JavaQuest.exe`, `/api/health` et une quête vérifiée) en réutilisant `tools/test-launcher.ps1` ; valider en l'exécutant.
