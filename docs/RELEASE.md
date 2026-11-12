# Distribution Windows — état et limites

## Construire
`bash tools/assemble.sh --zip` → `build/dist/JavaQuest.zip` (~44 Mo compressé, 84 Mo extrait) : `JavaQuest.exe` (lanceur), `app/runtime` (JRE jlink Temurin 25 : serveur, `javac`, modules du cours), `app/server.jar`, `app/runner/runner.jar`, `app/bin/jq-sandbox.exe` (broker AppContainer), `app/web`, `app/content`, licences, `LISEZMOI.txt`. Les supports ESIEE (`curriculum/originals/`) ne sont jamais embarqués.

## Tester
| Script | Ce qu'il prouve |
|---|---|
| `pwsh -NoProfile -File tools/test-dist.ps1 [-Build] [-Wsb]` | ZIP extrait dans un chemin avec espaces et accents ; contenu attendu, aucun support ni fichier sensible embarqué ; PATH réduit au système (ni Java ni Node) ; puis `tools/test-launcher.ps1` sur la copie extraite : démarrage, santé, Java exécuté en sandbox, contenu, quête vérifiée par son harnais (solution acceptée, code fautif refusé), double lancement, arrêt propre, arrêt brutal (Job kill-on-close), aucun processus résiduel. |
| `pwsh -NoProfile -File tools/test-wsb.ps1` | Même installation dans Windows Sandbox (Windows vierge, **réseau désactivé**, sans Java/Node), scénario exécuté par `wsb exec` (la CLI `wsb` n'ouvre pas de session : une `LogonCommand` ne s'exécute jamais) ; verdict par attentes explicites. |

Dernier résultat (2026-11-12) : `test-dist.ps1` → 15 PASS, 0 FAIL (lanceur 13/13). `test-wsb.ps1` → 4 PASS, 8 FAIL : ZIP extrait, PATH sans Java/Node, réseau coupé, mais **lancement bloqué par Smart App Control** (voir ci-dessous).

## Limites restantes
- **Exécutables non signés et Smart App Control (bloquant sur certains PC).** `JavaQuest.exe` et `app/bin/jq-sandbox.exe` ne sont pas signés (`javaw.exe` l'est, par Eclipse Foundation). Sur un Windows 11 où Smart App Control est actif (installations neuves ; c'est le cas de Windows Sandbox : `VerifiedAndReputablePolicyState = 1`), Windows refuse de les lancer : « Une stratégie de contrôle d'application a bloqué ce fichier ». Désactiver Smart App Control n'est pas une consigne acceptable pour des étudiants (irréversible sans réinstallation). Il faut soit signer avec un certificat reconnu (par exemple Azure Trusted Signing ou un certificat de signature de code d'une autorité reconnue), soit supprimer tout exécutable non signé de la chaîne de lancement. Décision à prendre (H1.3).
  - Constat en Windows Sandbox (Smart App Control actif, réseau coupé) : un `.cmd` non signé s'exécute, y compris marqué « téléchargé » (Zone.Identifier) ; `app/runtime/bin/java.exe` (signé) lance `server.jar` normalement ; seuls `JavaQuest.exe` et `jq-sandbox.exe` sont refusés (`CreateProcess error=4551`), et le serveur désactive alors l'exécution de code (`/api/health` : `sandbox:false`).
  - Conséquence : une architecture sans exécutable non signé est possible : lanceur porté par `javaw.exe` (signé), broker AppContainer/Job réécrit en Java (FFM, `java.base`) au lieu de `jq-sandbox.exe`.
- **SmartScreen.** Même sans Smart App Control, un exécutable non signé téléchargé (marque « Mark of the Web ») déclenche « Windows a protégé votre ordinateur » → « Informations complémentaires » → « Exécuter quand même ». La signature réduit ce problème, sans le supprimer tant que la réputation n'est pas établie.
- Windows 10 1703+ / 11 64 bits uniquement (AppContainer LPAC, voir `docs/SECURITY.md`).
