# Java Quest — application pédagogique ESIEE locale

## Démarrer (Windows)

**Double-clique sur `Démarrer Java Quest.cmd`.** Firefox/Chrome s'ouvrira automatiquement sur `http://127.0.0.1:8788`. Le serveur ne reçoit aucune connexion distante ; il écoute seulement l'interface `127.0.0.1`.

Prérequis : Node.js >=20 ; Java JDK 25 portable placé dans `.tools/jdk-25` (automatiquement détecté). Le Java système et le PATH n'ont pas été modifiés. Aucun abonnement, compte, service extérieur ou installation npm ne sont nécessaires au lancement.

Pour arrêter le serveur local : `Arrêter Java Quest.cmd`. La progression est conservée indépendamment de l'arrêt.

## Utilisation

1. Dans la carte d'aventure, choisis « Hello World, aventurier ! ».
2. Écris du code Java dans l'éditeur à coloration syntaxique, puis clique sur **Exécuter** pour voir la sortie.
3. Clique sur **Tester et valider** : le serveur compile ton code et exécute plusieurs cas de tests cachés lorsqu'ils existent.
4. Révise, débloque des niveaux, consulte les supports professoraux avec **Lire l'énoncé original complet**, retrouve tes essais dans **Mes progrès**.
5. **Mode examen** : session préparatoire sans indices ; elle ne reproduit pas encore toute l'annale ni tous ses contrôles automatiques.

## Important : limites de sécurité

Le moteur lance `javac` et `java` directement sur **ton propre PC Windows**, avec les droits de ton compte utilisateur. Il ne fournit **pas d'isolation forte**. Le timeout, la limitation de sortie et la mémoire JVM ne protègent pas complètement Windows des programmes dangereux. N'y exécute **que le code local que tu as personnellement écrit ou que tu considères fiable**. Ne copie jamais du code inconnu trouvé sur Internet et ne publie pas le serveur. Il n'écoute que `127.0.0.1`, impose l'origine identique, des limites de requête et des chemins de fichiers Java validés. Le compilateur utilise un dossier temporaire nettoyé après chaque tentative. Aucun fichier dans `Y:` n'est écrit.

## Sauvegarde

Les progrès sont écrits dans le navigateur (`localStorage`) puis sur le PC en `data/progress.json`, avec sauvegarde précédente `data/progress.json.bak`. Tu peux exporter et importer une sauvegarde JSON depuis l'application. Une réussite guidée ne vaut pas maîtrise. Les anciens scores restent conservés, mais une répétition de la même mission, même 24 heures plus tard, ne prouve plus la maîtrise : des preuves indépendantes distinctes et un transfert différé sont exigés. Le moteur de révisions adaptatives n'est pas encore en place.

## Sources et contenu

Les documents originaux ESIEE ont été recopiés en lecture seule vers `curriculum/originals/` pour consultation privée hors ligne, sans modifier les fichiers scolaires. Les activités sont marquées **ORIGINAL**, **GUIDÉ** ou **VARIANTE**. La matrice de traçabilité `curriculum/coverage.json` conserve 65 exigences initiales (non exhaustives), avec les sources et les activités liées, et la page Programme n'attribue jamais de couverture intégrale non vérifiée. L'application comporte actuellement **9 mondes et 37 activités**, dont **17 exercices de code** (un test de code ne démontre pas encore la maîtrise du sujet entier). L'annale de 2025 est disponible comme document original, mais ses annexes (notamment `domains.txt`) ne sont pas toutes fournies. Le programme total ESIEE n'est **pas** couvert à 100 %.

En novembre 2026, les variantes de boucles et de distance sont désormais testées sur plusieurs entrées, et un entraînement créé multi-fichiers est disponible. Le mode mission sur PC propose une scène 2D réagissant aux sorties Java réelles, des onglets Java et des raccourcis. Les mécanismes opérationnels et leurs limites sont détaillés dans `progress.md` et `PEDAGOGIE.md`.

D'autres activités peuvent être ajoutées de façon déclarative dans `public/catalog.js`; les tests d'évaluation automatique correspondants se définissent séparément dans `server.mjs`. Le site de cours public n'a pas été entièrement intégré.

## Développer et tester

`npm test` (21 tests au 11/11/2026) teste les contrats des données, les chemins de fichiers, les contraintes du serveur et les exercices enregistrés. `node server.mjs` démarre le serveur sans lanceur ; aucune installation npm n'est requise.

Le contrôle complet de la fidélité scolaire, de la sécurité du runner et de toutes les fonctions est encore à poursuivre avant de considérer Java Quest comme un produit final achevé. Consulter `plan.md`, `progress.md` et `MATRICE.md` pour reprendre le projet.
## Mises à jour du 11 novembre 2026

- **Boucle** : la réponse fixe `15` échoue ; cinq valeurs de N sont vérifiées sur du Java compilé. **Point** : la réponse fixe `5.0` échoue ; cinq essais de la classe `Point` et sa méthode `distance` sont exécutés via un harnais indépendant.
- **Évaluation** : le suivi conserve les tentatives, les indices et les solutions révélées ; les anciens XP ne sont pas effacés. Deux évaluations distinctes dont une variante de transfert après un délai sont désormais requises avant de conclure prudemment à la maîtrise. Il n'y a pas encore de planificateur de répétition espacée.
- **Éditeur desktop** : panneau de programmation élargi, console et scène 2D, contrôles visibles avant le défilement (1366×768 observé), `Ctrl+Entrée` exécute et `Ctrl+Maj+Entrée` teste, `Ctrl+S` sauvegarde. Onglets multi-fichiers : bouton `+ Fichier`, puis créer `Calculateur.java` pour la mission `Le laboratoire multi-fichiers`.
- **Sauvegarde** : état visible (et alerte si l'écriture disque échoue). Les données existantes ne sont pas recalculées en tant que nouvelles maîtrises. Sauvegarde préalable externe au dossier du jeu : `..\javaquest-backups\2026-11-11-avant-refonte`.
- **Tests** : 21 tests Node et tests API Java/Firefox dans une installation de test isolée, sans progression fictive sur le PC principal.
- **Déploiement** : <autre-machine> était hors ligne le 11/11 : la version installée sur ce second PC **n'a pas** été mise à jour.
