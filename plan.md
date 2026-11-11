# PLAN — JavaQuest 2.0

## Objectif final
RPG fantastique 2D (vue de dessus) jouable dans le navigateur, lancé par `JavaQuest.exe` depuis un ZIP Windows autonome hors ligne, où du **vrai Java compilé** (JDK 25, programme « Java EISC4 2026 ») agit sur le monde. Pédagogie fondée sur la recherche, maîtrise séparée des récompenses, mode examen, exécution du code utilisateur **réellement isolée**.

## Invariants
- Machine de développement : <machine> uniquement. `<autre-machine>` / `<ip-privée>` (PC d'un tiers) : jamais accédés, directement ou indirectement.
- Projet : `..\java`. Rollback : tag Git `legacy-v0.3` + copie `..\javaquest-backups\2026-11-11-avant-refonte`.
- Supports ESIEE (`curriculum/originals/`, copiés depuis Y:) : consultation privée, jamais versionnés, jamais dans le ZIP. Le jeu ne contient que du contenu original relié aux objectifs (provenance tracée). `data/progress.json` (progression personnelle) : préservé, jamais versionné.
- Code utilisateur = non fiable : exécution seulement dans AppContainer sans capacité + Job Object (mémoire, CPU, 1 processus, kill-on-close) + timeout ; jamais de repli silencieux non isolé. Toute limite de sécurité est documentée, jamais simulée.
- Serveur local : loopback uniquement, jeton de session, arrêt propre. Aucun service distant, compte, IA ou télémétrie au runtime.
- Assets/bibliothèques : licences vérifiées (CC0/MIT/OFL/GPLv2+CE), listées dans `THIRD_PARTY.md`.
- Une checkbox = preuve. XP ≠ maîtrise. Aucun seuil présenté comme scientifiquement démontré sans source.
- Travail sur branche `ai/javaquest-2`, commits par unité validée, push = checkpoint.

## Critères globaux de réussite
Les 18 critères d'acceptation du cahier des charges (ZIP autonome hors ligne ; lancement/arrêt ; monde multi-régions ; village/donjons/PNJ/quêtes ; vrai Java ; effets visibles ; progression ; objectifs ESIEE tracés ; corrections vérifiées ; débogage ; maîtrise ≠ récompenses ; révision ; examen ; sauvegardes ; licences ; sécurité vérifiée ; sans compte ; tests verts + limites signalées).

## Architecture retenue (audit A1)
- `launcher/` : C# .NET Framework 4.8 (présent sur Windows 10/11) → `JavaQuest.exe` (instance unique, port loopback libre, démarre le serveur, ouvre le navigateur, arrêt propre via Job kill-on-close) et `jq-sandbox.exe` (AppContainer + Job Object).
- `server/` : Java 25, `jdk.httpserver` — fichiers statiques, API (run/test/save), notation fiable, sauvegarde `%LOCALAPPDATA%\JavaQuest`. Remplace `server.mjs` (évite d'embarquer Node).
- `runner/` : Java exécuté DANS la sandbox — compilation en mémoire (`javax.tools`, `-proc:none`), exécution, API du monde (traces d'actions avec n° de ligne), harnais de tests.
- `web/` : Phaser 3 (moteur 2D), CodeMirror 5 (éditeur), modules ES natifs, sans build.
- `content/` : compétences, quêtes, exercices (énoncés originaux, solutions de référence, tests) validés par `tools/validate-content`.
- Runtime : `jlink` du JDK 25 Temurin (modules minimaux) embarqué dans le ZIP.

# Phase A — Audit et fondations

## A1 — Audit de l'existant, Git, continuité
### Objectif
Auditer Java Quest 0.3, décider conserver/améliorer/remplacer/abandonner, protéger la version initiale.
### Dépendances
Aucune.
### Tâches
- [x] A1.1 Identifier machine autorisée (<machine>) et emplacement du projet.
- [x] A1.2 Inventorier supports ESIEE disponibles (9 documents `curriculum/originals/`, Y: non monté, OneDrive sans cours Java).
- [x] A1.3 Git init, baseline + tag `legacy-v0.3`, dépôt GitHub privé `nevenfo/javaquest`.
- [x] A1.4 Rédiger `docs/AUDIT.md` (décisions par composant).
### Validation
- `git tag` contient `legacy-v0.3` ; `gh repo view nevenfo/javaquest --json visibility` = PRIVATE ; `docs/AUDIT.md` présent.

# Phase B — Moteur Java isolé

## B1 — Sandbox Windows (AppContainer + Job Object)
### Objectif
Exécuter une JVM non fiable sans accès fichiers personnels, réseau, création de processus, avec limites mémoire/CPU/temps.
### Dépendances
A1.
### Tâches
- [x] B1.1 `jq-sandbox.exe` : profil AppContainer dédié sans capacités, ACL lecture runtime, Job (mémoire, temps CPU, ActiveProcessLimit=1, UI limits, kill-on-close), timeout mural, sortie bornée.
- [x] B1.2 Suite d'attaques `tests/sandbox/` : lecture `%USERPROFILE%`, écriture, réseau (externe et loopback), `ProcessBuilder`, boucle infinie, bombe mémoire, bombe de threads, flood sortie, processus persistant.
### Validation
- `tools/test-sandbox` : toutes les attaques bloquées, programme légitime OK ; résultat consigné dans `docs/SECURITY.md`.

## B2 — Runner Java en sandbox
### Objectif
Compiler en mémoire et exécuter le code étudiant ; API du monde ; harnais de tests ; erreurs réelles javac/JVM avec lignes.
### Dépendances
B1.
### Tâches
- [x] B2.1 Compilation `javax.tools` en mémoire (multi-fichiers, Java 25, sources compactes `void main`), diagnostics réels.
- [x] B2.2 Exécution : stdin, `IO`, exceptions → lignes utilisateur, protocole de sortie à nonce.
- [x] B2.3 API du monde (`Hero`, `Magie`…) enregistrant actions + ligne source ; harnais de tests cachés.
### Validation
- Tests runner : code correct, erreur de compilation, exception, boucle infinie, trace d'actions correcte.

## B3 — Serveur Java local
### Objectif
Servir le jeu et l'API sur loopback, notation fiable, sauvegardes robustes.
### Dépendances
B2.
### Tâches
- [x] B3.1 HTTP 127.0.0.1, jeton de session, en-têtes de sécurité, limites de taille.
- [x] B3.2 `/api/run`, `/api/check` (tests de quête côté serveur), `/api/save` (atomique + .bak + versions de schéma) dans `%LOCALAPPDATA%\JavaQuest`.
### Validation
- Tests HTTP automatisés (refus hors loopback/jeton, save/load, run).

## B4 — Lanceur Windows
### Objectif
`JavaQuest.exe` : double-clic → jeu dans le navigateur ; arrêt propre.
### Dépendances
B3.
### Tâches
- [x] B4.1 Instance unique (mutex, réouverture navigateur), port libre, attente de santé, ouverture navigateur, fenêtre « Arrêter », erreurs lisibles.
### Validation
- Lancement depuis un chemin avec espaces, double lancement, arrêt : pas de processus résiduel.

# Phase C — Tranche verticale

## C1 — Premier scénario complet
### Objectif
Village jouable : déplacement ZQSD/flèches, collisions, PNJ, dialogue, une quête où le code Java ouvre une porte, vérification pédagogique.
### Dépendances
B3, assets (D1).
### Tâches
- [ ] C1.1 Phaser : carte tuilée, joueur animé, collisions, caméra, interactions.
- [ ] C1.2 Éditeur CodeMirror (coloration, lignes, indentation, erreurs cliquables, réinitialiser, brouillon conservé).
- [ ] C1.3 Quête « La porte du sanctuaire » : Java → compilation sandbox → trace → animation → tests serveur → récompense.
### Validation
- Test E2E navigateur headless (Edge CDP) : déplacement, dialogue, exécution Java, porte ouverte ; capture d'écran.

# Phase D — Systèmes communs

## D1 — Direction artistique et assets
### Objectif
Ensemble visuel/sonore cohérent sous licences compatibles.
### Dépendances
A1.
### Tâches
- [ ] D1.1 Télécharger/sélectionner packs Kenney (CC0), polices OFL, Phaser/CodeMirror (MIT) ; `THIRD_PARTY.md`.
### Validation
- Chaque fichier redistribué a sa licence listée ; `tools/check-licenses` OK.

## D2 — Interface de jeu complète
### Objectif
Menus, journal de quêtes, inventaire, carte, grimoire (compétences), sons, options clavier (AZERTY/QWERTY via `event.code`).
### Dépendances
C1.
### Tâches
- [ ] D2.1 Menu titre, profils locaux (plusieurs étudiants), sauvegarde/chargement.
- [ ] D2.2 Journal de quêtes, inventaire, carte du monde, grimoire de compétences.
- [ ] D2.3 Aides progressives (indices gradués tracés) et débogage visuel (pas-à-pas des actions avec ligne, valeurs `Debug.watch`).
### Validation
- Tests unitaires état/sauvegarde (node --test) + E2E menus.

# Phase E — Moteur pédagogique

## E1 — Référentiel de compétences
### Objectif
Micro-compétences issues des supports ESIEE avec prérequis, erreurs fréquentes, critères, provenance.
### Dépendances
A1.2, R1.
### Tâches
- [ ] E1.1 `content/skills.json` + validateur (graphe acyclique, provenance obligatoire).
### Validation
- `tools/validate-content` OK ; chaque compétence reliée à ≥1 source ou marquée « complément ».

## E2 — Maîtrise, révision, adaptation
### Objectif
Modèle de preuves (guidé/autonome/transfert/différé), réactivation espacée, choix d'activités.
### Dépendances
E1, R1.
### Tâches
- [ ] E2.1 Modèle de maîtrise par niveaux de preuve, sans inférence sur une seule erreur.
- [ ] E2.2 Planificateur de réactivation (intervalles = hypothèse de conception paramétrable, documentée).
### Validation
- Tests unitaires du modèle (cas : aide utilisée, réussite unique, transfert différé, échecs isolés).

## R1 — Synthèse scientifique
### Objectif
`docs/PEDAGOGIE.md` : règles de conception, force de preuve, hypothèses testables.
### Dépendances
Aucune.
### Tâches
- [x] R1.1 Recherche (web-research) et synthèse sourcée.
### Validation
- Chaque règle cite ses sources et sa force de preuve.

# Phase F — Monde et contenu

## F1 — Régions, donjons, quêtes
### Objectif
Village + régions couvrant le programme : bases (variables, types, conditions, boucles, méthodes, String, args/IO, exceptions) ; objets (classes, records, encapsulation, equals/hashCode/toString, static) ; hiérarchies (interfaces, default, héritage, Comparable) ; tableaux & collections (ArrayList, LinkedList, HashSet/TreeSet, HashMap, tri, Comparator) ; projet REPL type JaList.
### Dépendances
C1, D2, E1, E2.
### Tâches
- [ ] F1.1 Région 1 + donjon (bases).
- [ ] F1.2 Région 2 + donjon (objets/records).
- [ ] F1.3 Région 3 + donjon (interfaces/hiérarchies).
- [ ] F1.4 Région 4 + donjon (tableaux/collections).
- [ ] F1.5 Région finale (projet intégrateur, compagnon programmable).
### Validation
- Toutes les solutions de référence compilent et passent leurs tests ; des solutions fautives connues échouent (`tools/validate-content`) ; E2E de traversée.

## G1 — Mode examen
### Objectif
Sujets type examen (temps, aides limitées), énoncé original du joueur accessible si le fichier local existe, sans redistribution.
### Dépendances
F1.
### Tâches
- [ ] G1.1 Mode examen chronométré, sans indices, notation par tests, bilan par compétence.
### Validation
- Tests : examen complet passé avec solutions de référence ; aides désactivées vérifiées.

# Phase H — Distribution

## H1 — ZIP Windows autonome
### Objectif
ZIP : extraire → double-clic `JavaQuest.exe` → jouer, hors ligne, sans prérequis ni admin.
### Dépendances
B4, F1, G1.
### Tâches
- [ ] H1.1 `tools/build-dist` : jlink runtime, compilation, copie web/contenu, ZIP.
- [ ] H1.2 Test depuis un autre dossier (chemin avec espaces), réseau coupé pour le processus, sans Java/Node dans le PATH ; Windows Sandbox si disponible.
### Validation
- Script `tools/test-dist` vert ; rapport dans `docs/RELEASE.md` avec limites restantes.
