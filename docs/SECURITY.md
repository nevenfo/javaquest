# Sécurité de l'exécution du code des joueurs

Le code Java saisi est **non fiable**. Il n'est jamais exécuté avec les droits de la session Windows.

## Mécanisme (`launcher/Sandbox`, `jq-sandbox.exe`)
1. **AppContainer LPAC** (`JavaQuest.Sandbox.v1`, mode « Less Privileged AppContainer ») sans capacité réseau ; seule capacité : `registryRead` (nécessaire au chargement de la JVM).
   - Pas d'accès aux fichiers de l'utilisateur (profil, Documents, Bureau, OneDrive…) : l'accès exige une ACE explicite pour le SID du conteneur.
   - Lecture/exécution accordée **uniquement** au runtime Java du jeu et au dossier des classes du runner (`jq-sandbox grant`, ACE héritée RX, sans droits admin).
   - Écriture seulement dans le dossier privé du conteneur (`%LOCALAPPDATA%\Packages\javaquest.sandbox.v1\AC`).
   - Réseau : création de socket refusée (externe, DNS, loopback, écoute).
   - Registre utilisateur (HKCU) : refusé.
2. **Politique noyau** `PROCESS_CREATION_CHILD_PROCESS_RESTRICTED` : création de processus interdite.
3. **Job Object** : 1 processus actif, mémoire totale plafonnée, temps CPU plafonné, plafond de 50 % CPU, restrictions UI (presse-papiers, bureau, paramètres système, atomes, handles), `KILL_ON_JOB_CLOSE` (si le broker meurt, la JVM meurt), arrêt sur exception non gérée.
4. **Délai mural** + **plafond de sortie** (le job est terminé en cas de dépassement).
5. **Environnement minimal** : seules les variables système/profil exigées par Windows ; aucun jeton ni PATH utilisateur. Liste blanche de handles hérités (stdin/stdout/stderr uniquement).
6. Pas de repli : si la sandbox ne peut pas être créée (ACL impossible, Windows trop ancien), **l'exécution est refusée** avec un message explicite.

## Vérification
`bash tools/test-sandbox.sh` — programme hostile `tests/sandbox/Attacks.java`. Résultat du 2026-11-11 sur Windows 11 Pro 26200 : **26 PASS / 0 FAIL** (lecture/écriture profil bloquées, réseau externe/loopback/DNS/écoute bloqués, processus bloqués, HKCU bloqué, boucle infinie → limite CPU, bombe mémoire → limite job, bombe de threads → limite mémoire, flood → limite sortie, aucun processus résiduel).

## Limites connues (assumées et documentées)
- Les fichiers système **publics** lisibles par `ALL RESTRICTED APPLICATION PACKAGES` (ex. `C:\Windows\win.ini`, liste de `C:\Program Files`) restent lisibles. Ils ne contiennent pas de données personnelles.
- Le code peut consommer jusqu'aux plafonds (CPU 50 %, mémoire, durée) pendant quelques secondes.
- Isolation fondée sur Windows 10 1703+ / 11 (AppContainer LPAC, politique d'enfants). Sur un système plus ancien, l'exécution est refusée.
- Sur un volume sans ACL (FAT32/exFAT, ex. clé USB), le contrôle d'accès fichier ne s'applique pas au dossier du jeu lui-même ; les fichiers du profil restent protégés (NTFS).
- Le Java Security Manager n'est pas utilisé (supprimé du JDK) ; aucune garantie ne repose sur lui.
- Un joueur peut tromper la notation de **son propre** code (ex. reproduire la sortie attendue) ; ce n'est pas une faille de sécurité, mais la notation fiable est faite par le serveur avec des entrées cachées variées.
