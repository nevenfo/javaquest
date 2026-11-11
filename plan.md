# Java Quest — décisions durables

- Emplacement confirmé par l'utilisateur : `..\java` ; jamais sur le lecteur Y:.
- Stack : navigateur HTML/CSS/JavaScript natif sans installation de bibliothèques au lancement ; Node.js local ; JDK Temurin 25 portable. Choix d'une base légère adapté à Windows et à l'usage totalement hors ligne ; la stack React/Vite/CodeMirror suggérée était indicative, non impérative.
- Interface : 9 mondes, 35 activités et éditeur à coloration syntaxique. Données pédagogiques : `public/catalog.js`. Exigences : `curriculum/coverage.json` / `MATRICE.md`.
- Sources : original ESIEE lu sur Y: et recopié en consultation privée locale ; toute modification JaList réelle est exclue.
- Moteur : `javac/java` réels, argument list sans shell, limites de durée et de sortie. Important : pas d'isolation Windows forte ; code personnel de confiance seulement. À ne pas transformer en service accessible au réseau.
- Sauvegarde : localStorage + JSON local atomique et fichier `.bak`, export/import.
- Validation pédagogique : une réussite aidée n'établit aucune maîtrise ; ni XP ni quantité de clics ne démontrent la préparation à l'examen.
- Définition de « couvert » : source vérifiée, leçon, pratique pertinente, tests réels, évaluation autonome, provenance. Les liens à une mission ne satisfont pas seuls la couverture.
## Décisions mises en œuvre au 2026-11-11
- Conserver Node.js + navigateur et le JDK25 portable ; refondre une mission desktop en priorité sans remplacement global de stack.
- La sortie de Java réel pilote une animation de robot 2D sur les missions pertinentes ; l’animation reflète un résultat, tandis que les tests cachés vérifient la correction.
- Séparer récompense de participation (XP) et maîtrise : l'ancienne répétition à 24h ne suffit plus, conserver l'historique et exiger des preuves de transfert autonomes distinctes. Ne pas inventer de validation scientifique.
- Autoriser le code Java multi-fichiers avec un état local persistant ; toujours préserver les copies des énoncés scolaires.
- Source de vérité de développement : PC <machine> ; la copie de <autre-machine> reste non examinée en raison du statut hors ligne.
