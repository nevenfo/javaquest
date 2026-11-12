# PROGRESS

## Phase actuelle
Phases A–G validées. Reste : H1 (H1.1 fait ; H1.2 partiel ; H1.3 en attente de décision).

## Tâche actuelle
H1.3 — rendre le lancement compatible avec Smart App Control.

## Dernière tâche validée
H1.1 (+ H1.2 partiel) : `tools/test-dist.ps1` (ZIP extrait dans un chemin avec espaces/accents, PATH sans Java/Node, lanceur + quête vérifiée) ; `tools/test-wsb.ps1` passe par `wsb exec` (LogonCommand jamais exécutée par la CLI) et rend un verdict.
Validation :
- `pwsh -NoProfile -File tools/test-dist.ps1` → 15 PASS, 0 FAIL (lanceur 13/13).
- `pwsh -NoProfile -File tools/test-wsb.ps1` → 4 PASS, 8 FAIL : Smart App Control bloque `JavaQuest.exe` et `jq-sandbox.exe` non signés (détail et expériences : `docs/RELEASE.md`).
- D2 : `npm run -s test:web` 33/33, `node tools/e2e.mjs` 33/33.

## Décisions actives
- H1.3 (décision utilisateur 2026-11-12) : distribution gratuite, portable, hors ligne, Win10/11 avec Smart App Control actif, sans certificat payant, sans admin. Voie : remplacer `JavaQuest.exe` et `jq-sandbox.exe` par du Java FFM exécuté par le `java(w).exe` signé, **isolation AppContainer LPAC + Job Object intégralement préservée**. Prototype minimal validé avant refonte ; si l'équivalence de sécurité n'est pas démontrée (`tools/test-sandbox.sh` 26/26 + WSB 12/12), garder l'architecture actuelle et présenter signature / MSIX Store.
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
Aucun (décision H1.3 prise).

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` · `runner/` · `launcher/` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e.mjs,coverage.mjs,test-launcher.ps1}` · `docs/`

## NEXT ACTION
H1.3 — Prototype FFM minimal du broker (`grant` + `run --lpac` : AppContainer LPAC, politique sans enfants, Job Object) lancé par `java.exe` signé, avec la même CLI que `jq-sandbox.exe` ; valider par `tools/test-sandbox.sh` pointé sur le prototype (26/26 attendu).
