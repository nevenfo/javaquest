# Distribution Windows — état et limites

## Principe
Gratuite, portable, hors ligne, sans installation ni droits administrateur, compatible Windows 10 (1703+) / 11 64 bits **y compris avec Smart App Control actif**. Aucun exécutable non signé n'est livré : le seul code natif exécuté est le runtime Java Temurin 25 (`java.exe`/`javaw.exe` signés par Eclipse Foundation) et les DLL de Windows ; tout le reste est du Java (`.jar`) ou du texte.

## Construire
`bash tools/assemble.sh --zip` → `build/dist/JavaQuest.zip` (~44 Mo compressé, 84 Mo extrait) :
- `JavaQuest.cmd` — point d'entrée (script texte) : `start` du `javaw.exe` signé sur `fr.javaquest.server.Launch` ;
- `app/runtime` — JRE jlink (serveur, `javac`, modules du cours) ;
- `app/server.jar` — serveur local + lanceur (instance unique, navigateur via `rundll32 url.dll`, arrêt par « Menu > Quitter le jeu » ou après 4 min sans onglet ouvert) ;
- `app/bin/sandbox.jar` — broker d'isolation (Java FFM : AppContainer LPAC + Job Object, voir `docs/SECURITY.md`) ;
- `app/runner/runner.jar`, `app/web`, `app/content`, licences, `LISEZMOI.txt`.

Les supports ESIEE (`curriculum/originals/`) ne sont jamais embarqués.

## Tester
| Commande | Ce qu'elle prouve |
|---|---|
| `bash tools/test-sandbox.sh` | 26 attaques réelles contre le broker livré (fichiers, réseau, DNS, écoute, processus, registre, CPU, mémoire, threads, sortie, processus résiduels). |
| `pwsh -NoProfile -File tools/test-dist.ps1 [-Build] [-Wsb]` | ZIP extrait dans un chemin avec espaces et accents ; contenu attendu, aucun `.exe` hors du runtime, aucun support ni fichier sensible ; PATH réduit au système (ni Java ni Node) ; puis `tools/test-launcher.ps1` sur la copie : démarrage, santé + sandbox, Java exécuté, quête acceptée / code fautif refusé, double lancement (une seule instance), `/api/quit` refusé sans jeton puis accepté, kill brutal puis relance (verrou libéré), arrêt sur inactivité. `-Wsb` : `tools/test-wsb.ps1`. |
| `pwsh -NoProfile -File tools/test-wsb.ps1` | Installation dans Windows Sandbox (Windows vierge, **réseau désactivé**, sans Java/Node, **Smart App Control actif**), en session utilisateur réelle (`wsb connect` + `wsb exec -r ExistingLogin`) : 14 attentes explicites. |

Dernier résultat (2026-11-12) : `test-sandbox.sh` 26/26 ; `test-dist.ps1 -Wsb` 17/17 (lanceur 23/23, Windows Sandbox 14/14 : serveur démarré, sandbox prête, Java exécuté, fichier personnel refusé au code, 143 quêtes, arrêt propre sans processus résiduel, `Politique CI : 1`, 0 exe non signé livré).

## Limites restantes
- **SmartScreen / avertissement de fichier téléchargé.** Un `.cmd` issu d'un ZIP téléchargé peut afficher un avertissement (« Éditeur inconnu ») au premier double-clic : « Exécuter ». Smart App Control, lui, ne le bloque pas (vérifié, y compris marqué Zone.Identifier). Signer n'est pas nécessaire.
- Pas de fenêtre native (le runtime n'embarque pas `java.desktop`) : le jeu se quitte depuis son menu, ou s'arrête seul 4 min après la fermeture de l'onglet (option `--idle-seconds`). Après une longue mise en veille, il peut falloir relancer `JavaQuest.cmd` (aucune perte : sauvegarde continue).
- Surcoût du broker Java : ~90 ms par exécution de code (démarrage d'une JVM légère).
- Windows 10 1703+ / 11 x64 uniquement (AppContainer LPAC ; offsets de structures x64).
- Windows Sandbox : en contexte `System` (session 0), tout processus AppContainer échoue (`0xC0000142`) ; les tests utilisent donc une session utilisateur.
