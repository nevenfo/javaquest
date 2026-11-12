# Écrire du contenu JavaQuest (quêtes)

Chaque quête est un dossier `content/quests/<id>/` (l'ordre alphabétique des dossiers = ordre d'affichage ; préfixer par la région : `v05-…`, `f01-…`, `m01-…`, `a01-…`, `t01-…`, `b01-…`, `c01-…`, `r-…` pour les réactivations).

## quest.json (champs)
| Champ | Rôle |
|---|---|
| `id` | identique au nom du dossier |
| `title`, `summary` | titre court ; résumé d'une phrase (journal, tableau des primes) |
| `region` | `village` · `foret` · `mines` · `artisans` · `tour` · `bibliotheque` · `citadelle` · `crypte` (donjon optionnel de la région 1, `main: false`, préfixe `d1-`) · `fonderie` (donjon optionnel de la région 2, `main: false`, préfixe `d2-`) · `labyrinthe` (donjon optionnel de la région 3, `main: false`, préfixe `d3-`) |
| `giver` | PNJ qui donne la quête (voir liste ci-dessous) — obligatoire sauf réactivations (`assessment: review`) |
| `main` | `true` pour la quête principale (chaîne obligatoire de la région) |
| `requires` | ids de quêtes à terminer avant (définit l'ordre) |
| `kind` | `code` (écrire), `fix` (réparer un code fourni dans `starter`), `parsons` (ordonner), `predict` (prédire la sortie), `quiz` (QCM) |
| `assessment` | `guided` (exemple/complétion), `practice`, `autonomous` (seul, sans structure fournie), `transfer` (même notion, contexte nouveau), `review` (réactivation espacée, sans giver), `exam` |
| `skills` | ids de `content/skills.json` (1 à 3) |
| `source` | objectif du cours ESIEE relié (paraphrase courte, jamais le texte du sujet) |
| `brief` | énoncé (markdown simple : `code`, **gras**, listes `- `, blocs ```), avec un exemple d'entrée/sortie |
| `lesson` | facultatif : exemple résolu commenté (différent de la solution !) |
| `hints` | 2–3 indices gradués : rappel du concept → où regarder → exemple analogue. **Jamais la solution complète.** |
| `starter` | `{ "Main.java": "…" }` code de départ (guidé : avec `/* ??? */` ; autonome : quasi vide) |
| `support` | fichiers Java fournis visibles en lecture seule (API de la quête), compilés avec le joueur |
| `harness`, `solutions`, `wrong` | harnais de vérification ; ≥ 1 solution de référence (liste de listes de fichiers) ; ≥ 1 solution fautive plausible (erreur typique). Jamais envoyés au client. |
| `usesStdin`, `sampleStdin` | affiche le champ Entrée ; valeur d'essai |
| `multiFile` | autorise l'ajout de fichiers (classes séparées) |
| `parsons` | `{ "prefix": "void main() {\n", "lines": [...dans l'ordre...], "distractors": [...], "suffix": "}" }` ; indentation = 1 niveau par défaut sous le prefix |
| `program`, `expected`, `stdin` | (predict) programme montré, sortie exacte attendue (vérifiée par `Validate`) |
| `questions`, `answerKey`, `explanations` | (quiz) `[{q, options:[…], multi:bool}]`, clé `[[indices]]`, explication affichée après bonne réponse |
| `stage` | scène du monde : `{ "speak": true, "actors": { "golem": { "sprite": 109, "x": 2, "y": 0 } } }` (coordonnées relatives au héros ; sprites = index Tiny Dungeon : 84 mage, 85 garçon, 99 fille, 108 slime, 109 golem/cyclope, 110 diablotin, 120 chauve-souris, 121 fantôme, 122 araignée, 123 rat, 124 squelette, 89 coffre, 113–116 potions) |
| `dialog` | `intro` [pages], `resume`, `success` [pages], `done`, `locked` |
| `effects.onPass` | `flag:<nom>` · `item:<id>:<nom>` · `unlock:<texte>` · `door:<id>` |
| `reward.xp` | 10–60 |

**Flags de fin de région (obligatoires sur la dernière quête principale)** : `f-foret`, `f-mines`, `f-forge` (artisans), `f-tour`, `f-bibliotheque`, `f-citadelle`. Donjon optionnel : `f-crypte` (posé par d1-04, jamais requis par la chaîne principale ; accès par le portail mines → crypte, `requires: 'f-mines'`) ; `f-fonderie` (posé par d2-04 ; accès par le portail artisans → fonderie, `requires: 'f-forge'`) ; `f-labyrinthe` (posé par d3-04 ; accès par le portail tour → labyrinthe, `requires: 'f-tour'`).

## PNJ disponibles (`giver`)
village : `ysolde`, `pip`, `garde`, `meunier`, `lina` · foret : `sylve`, `gobelin` · mines : `mineur`, `spectre` · artisans : `brom`, `rosa`, `pompiste` · tour : `sentinelle`, `mage` · bibliotheque : `lyra` · citadelle : `examinateur` · crypte : `gardien`, `golem` · fonderie : `fondeur`, `gabarit` · labyrinthe : `heraut`, `sphinx`.

## Harnais (`harness/Verif.java`)
```java
import fr.javaquest.runner.Check;
class Verif {
    static void verifier() {
        Check.test("nom lisible du test", () -> {
            String out = Check.runMain("Main", "entrée\n", "arg0");   // sortie (l'entrée est en écho comme dans un terminal)
            Check.equal(attendu, obtenu, "quoi");                       // message : « quoi : attendu X, obtenu Y »
            Check.isTrue(condition, "message pédagogique");
            Check.near(3.14, valeur, 1e-9, "quoi");
            var lignes = Check.lines(out);                              // lignes non vides, sans espaces de fin
            Class<?> c = Check.playerClass("Point");                    // réflexion (structure : champs private/final…)
        });
    }
}
```
Le harnais peut appeler directement les classes/méthodes du joueur (`new Point(1, 2).distanceTo(...)`) : s'il ne compile pas avec le code du joueur, le jeu affiche « structure attendue manquante ». Règles :
- **Varier les entrées** (≥ 4 cas dont des cas limites : 0, négatif, vide, un élément) pour qu'une réponse codée en dur échoue.
- Ne pas exiger de format non annoncé dans `brief`. Prompt de `IO.readln("…")` toléré : comparer les **dernières** lignes.
- Messages d'échec informatifs (P7) sans donner la solution.

## Rendre le code visible dans le monde
Les sorties s'affichent dans une bulle au-dessus du héros. Pour des effets plus riches, une classe `support` appelle `fr.javaquest.runner.Trace.event(nom, args...)` :
`acteur.deplacer(id, dx, dy)` · `acteur.dire(id, texte)` · `acteur.apparaitre(id, sprite, x, y)` · `acteur.disparaitre(id)` · `acteur.sprite(id, sprite)` · `tuile.poser(x, y, gid)` (gid Tiny Town = index+1, Tiny Dungeon = index+133) · `effet(type, x, y)` (etincelle, feu, eau, fumee, coeur, eclair, magie, poison) · `heros.dire(texte)` · `camera(x, y)` · `compteur(nom, valeur)`.
Chaque événement est rejoué dans l'ordre avec surlignage de la ligne du joueur qui l'a provoqué. `Sonde.voir("nom", valeur)` (toujours disponible) affiche une valeur observée.

## Progression pédagogique par compétence (docs/PEDAGOGIE.md)
Pour chaque compétence centrale : 1 activité guidée (exemple + complétion ou parsons/prédiction) → 1 autonome → 1 transfert (autre contexte) → 1–2 réactivations `review` (`r-<skill>-<n>`, sans giver). Entrelacer les notions proches dans les quêtes de transfert/donjon. Énoncés **originaux** inspirés des objectifs ESIEE (jamais copiés).

## Validation
`bash tools/build.sh && .tools/jdk-25/bin/java -cp build/server/server.jar fr.javaquest.server.Validate --content content --runtime .tools/jdk-25 --sandbox launcher/Sandbox/bin/Release/jq-sandbox.exe --runner build/runner/runner.jar --data build/testdata [--only <id>]`
Toute quête doit être `OK` : solutions acceptées, fautives refusées, départ refusé, prédiction exacte, clé de quiz cohérente.
