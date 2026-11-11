# Java Quest — état d'avancement au 2026-11-11

## Réalisé et vérifié le 2026-11-11
- Copie source sélectionnée : <machine>, `..\java`. La copie <autre-machine> n'était pas joignable par NevenLink le 11/11 : ni comparée ni modifiée ni déployée.
- Sauvegarde préalable réelle : `..\javaquest-backups\2026-11-11-avant-refonte` (code, programme, données et lanceurs). Empreinte SHA-256 initiale de `data/progress.json` : `5FA8D71F5896F36070CC2D86144E158CD9C4ACA131798230C87F1AA60F2773FC`. Recontrôler cette empreinte après la remise en service.
- Défauts reproduits AVANT modification : `first-loop` validait la constante `15`, `point-distance` la constante `5.0`. APRÈS : constants refusées ; boucle authentique admise sur 5 valeurs différentes ; classe `Point` et méthode `distance(Point)` testées depuis un programme-témoin sur 5 jeux de coordonnées ; 4 cas pour `energy-transfer`.
- Amélioration prudente du suivi : `public/mastery.js` refuse la maîtrise par répétition d'un seul exercice, réponse théorique seule, indices/correction révélée, ancien historique sans preuves et tentative IA déclarée. Exige au moins deux activités de programmation autonomes distinctes dont un transfert différé d'au moins 24 h. Cette durée est une règle locale conservatrice, PAS une cadence scientifiquement optimale. Les anciens XP/réussites restent conservés ; les anciennes maîtrises sans preuves sont déclassées dans l'affichage, non supprimées des données.
- UX desktop réellement constatée sur Firefox 1366×768 : sidebar réduite en mission, titre/consigne/éditeur Java/scene 2D/console et actions visibles à l'ouverture ; raccourcis Ctrl+Entrée exécuter, Ctrl+Maj+Entrée tester, Ctrl+S sauvegarder ; code en 15px et numéros de lignes. Scène 2D réagit à la vraie sortie Java, sans interpréter celle-ci comme HTML.
- Éditeur graphique multi-fichiers : onglets de fichiers Java, création/suppression sous confirmation, édition, persistance `draftFiles` compatible avec l'ancien `drafts`. Le nouveau défi `multi-file-practice` exige `Main.java` + `Calculateur.java`, testé avec 4 paires de nombres : compilation valide acceptée, projet à un fichier refusé. Test GUI de bout en bout effectué dans une copie **isolée** `..\javaquest-tests\ui-20261111`, progression de simulation NON transférée vers la source.
- Alerte visible si sauvegarde distante vers `data/progress.json` échoue. Écriture serveur atomique préexistante conservée, schéma de sauvegarde actuel toujours version 1.
- Catalogue actuel : 9 mondes, 37 activités, 17 exercices de code Java. Ancien référentiel ESIEE : 65 exigences identifiées, 31 rattachées au moins partiellement, couverture intégrale prouvée : 0, inventaire non exhaustif.
- **21 tests Node automatisés réussis** après mise à jour. Essais API Java réels et essai GUI validés ; les deux anciens faux positifs échouent, les programmes corrects passent. Aucune compétence personnelle de Neven n'a été évaluée dans ces tests techniques.

## Limites non résolues
- Pas de démonstration de couverture intégrale du programme ESIEE ni de relecture exhaustive des sous-questions ; vrais sujets et JaList multi-modules encore incomplets.
- Révisions différées adaptatives, diagnostic détaillé et enchaînement pédagogique complet non implémentés ; le nouveau critère de maîtrise est conservateur mais pas une validation scientifique du produit.
- L'éditeur est encore un textarea avec surbrillance et onglets, pas CodeMirror : pas de complétion sophistiquée, diagnostics directement sur les lignes ni gestion complète des packages.
- Sauvegarde : double stockage navigateur/disque, sans système de fusion complet ; migration versionnée, restauration après redémarrage physique et jeux de données corrompus non encore vérifiés.
- 1920×1080, 2560×1440, zoom 125–150 %, navigation clavier complète, WCAG AA et performances au long cours non vérifiés.
- Code Java toujours lancé avec les droits du compte Windows et **sans sandbox forte**. Exécuter uniquement du code local de confiance ; port loopback seulement. La scène 2D est une première tranche, non la refonte intégrale des mondes.
- Deuxième PC hors ligne : pas de synchronisation ni de déploiement.

## Reprise
**NEXT ACTION UNIQUE :** construire puis tester un moteur de révisions différées et de transfert par compétence avec stockage robuste et migration, en conservant les originaux ESIEE et toutes les progressions ; poursuivre ensuite la cartographie du programme et les examens réels. La cible distante ne doit être déployée qu'après sa reconnexion et comparaison de ses données.
