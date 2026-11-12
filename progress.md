# PROGRESS

## Phase actuelle
Toutes les phases du plan sont validées (A–H). Livraison : PR `ai/javaquest-2` → `main`.

## Tâche actuelle
Aucune tâche de développement ouverte ; fusion de la PR en attente de l'utilisateur.

## Dernière tâche validée
H1.4 : crédit d'auteur « Nevant » (écran titre, Crédits, README, LISEZMOI, `package.json`, manifestes `.jar` via `tools/author.mf` et `sandbox/manifest.mf`).
Validation :
- `npm run -s test:web` 33/33 ; `bash tools/test-server.sh` 143 quêtes ; `node tools/e2e.mjs` 36/36 ; `bash tools/test-sandbox.sh` 26/26 (1 échec isolé non reproduit sur 5 passages : instabilité de timing à surveiller).
- `pwsh -NoProfile -File tools/test-dist.ps1 -Build -Wsb` → 17/17 (Windows Sandbox 14/14) ; `build/dist/JavaQuest.zip` régénéré avec le crédit.

## Décisions actives
- Windows Sandbox : exécuter les scénarios en session utilisateur (`wsb connect` puis `wsb exec -r ExistingLogin`) ; en contexte `System` (session 0), tout enfant AppContainer échoue en `0xC0000142`. Sous PowerShell 5, quoter les options `-XX:...` passées à un exe.
- H1.3 (décision utilisateur 2026-11-12) : distribution gratuite, portable, hors ligne, Win10/11 avec Smart App Control actif, sans certificat payant, sans admin. Réalisé : aucun exe non signé livré ; isolation AppContainer LPAC + Job Object préservée (équivalence démontrée : 26/26 + WSB 14/14). Toute évolution du broker doit repasser ces deux suites.
- Architecture : `plan.md` § Architecture retenue, `docs/AUDIT.md`. Sécurité : `docs/SECURITY.md`. Pédagogie : `docs/PEDAGOGIE.md`. Contenu : `docs/CONTENT.md`, couverture générée `docs/COUVERTURE.md`.
- Région finale `sommet` : API du compagnon dans `docs/CONTENT.md` § « Le compagnon de l'Observatoire ».
- Donjons : quêtes `d<n>-NN` optionnelles (`main: false`), région = carte du donjon, accès gardé par le flag de fin de région, dernière quête pose `f-<donjon>` ; vérifiées dans `tools/e2e-traversee.mjs` après la chaîne principale.
- Quêtes citadelle `c01`–`c04` : optionnelles (`main: false`), chaînées par `requires` ; accès via la porte du sanctuaire.
- Runner : stdin servi ligne par ligne avec écho ; `-Dstdin.encoding=UTF-8` ; args saisis dans l'atelier (`ed-args`) si `usesArgs` ou compétence `base.args`.
- Sprites de PNJ : uniquement des index de personnages déjà nommés dans `SPRITES` (101–107 sont des objets) ; libres : slime 108, bat 120, rat 123.
- Toute nouvelle carte doit passer `tests/web/maps.test.mjs` (le vide est solide ; relier les salles par `carve`).
- E2E : Edge headless + CDP, émulation de focus obligatoire ; `waitFor` échoue si l'évaluation lève une exception.
- Auteur affiché : « Nevant » ; pas de signature numérique (aucun certificat, H1.3).
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun (décision H1.3 prise).

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` (dont `Launch.java`, `SandboxRunner.java`) · `sandbox/` · `runner/` · `launcher/JavaQuest.cmd` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e,e2e-traversee,coverage}.mjs` · `tools/{test-dist,test-launcher,test-wsb}.ps1` · `docs/` (`RELEASE.md`, `SECURITY.md`)

## NEXT ACTION
H1 — Faire relire puis fusionner par l'utilisateur la PR `ai/javaquest-2` → `main` ; après fusion, publier `build/dist/JavaQuest.zip` (régénéré par `pwsh -NoProfile -File tools/test-dist.ps1 -Build -Wsb`, attendu 17/17).
