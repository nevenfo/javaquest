# PROGRESS

## Phase actuelle
Phases A–H livrées. Dépôt public `nevenfo/javaquest` (historique réécrit), release `v2.0.0` avec `JavaQuest.zip`.

## Tâche actuelle
H1.6 (fiabilité de `tools/test-sandbox.sh`), facultative.

## Dernière tâche validée
H1.5 : publication publique ; H1.4 : crédit d'auteur « Nevant ».
Validation :
- Historique public : 0 occurrence de chemin/machine/tiers/e-mail personnels ; recouvrement 9-grammes avec `curriculum/originals/` limité à des titres de TD.
- `pwsh -NoProfile -File tools/test-dist.ps1 -Build -Wsb` → 17/17 ; test-web 33/33 ; test-server 143 quêtes ; e2e 36/36 ; test-sandbox 26/26 (1 échec isolé non reproduit, cf. H1.6).

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
- Dépôts : `origin` = `nevenfo/javaquest` (PUBLIC, historique nettoyé) ; `archive` = `nevenfo/javaquest-archive` (PRIVÉ, historique d'origine avec extraits du sujet d'examen : ne jamais rendre public ni pousser vers `origin`). Sauvegarde : `..\javaquest-backups\2026-11-12-historique-complet-avant-reecriture.bundle`. E-mail Git du dépôt : noreply GitHub.
- Supports ESIEE : `curriculum/originals/` (jamais versionnés ni distribués).

## Blocage actif
Aucun.

## Incident à signaler à l'utilisateur
`javaquest-install.zip` (non versionné) supprimé par erreur le 2026-11-11 ; archive reconstituée : `..\javaquest-backups\javaquest-install-RECONSTRUIT-depuis-legacy-v0.3.zip`.

## Fichiers / zones utiles
`web/js/` · `content/{skills.json,quests/}` · `server/src/fr/javaquest/` (dont `Launch.java`, `SandboxRunner.java`) · `sandbox/` · `runner/` · `launcher/JavaQuest.cmd` · `tools/{build,assemble,test-server,test-sandbox}.sh` · `tools/{e2e,e2e-traversee,coverage}.mjs` · `tools/{test-dist,test-launcher,test-wsb}.ps1` · `docs/` (`RELEASE.md`, `SECURITY.md`)

## NEXT ACTION
H1.6 — Lancer `bash tools/test-sandbox.sh` 20 fois, identifier le test qui échoue par intermittence, corriger, puis revalider 20/20 à 26/26.
