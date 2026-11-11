# PROGRESS

## Phase actuelle
Phase B — Moteur Java isolé (B1 validé, B2 à faire).

## Tâche actuelle
B2.1 — Runner Java en sandbox : compilation en mémoire.

## Dernière tâche validée
B1 — `jq-sandbox.exe` (AppContainer LPAC + capacité `registryRead` + Job Object + politique sans enfants).
Validation :
- `bash tools/test-sandbox.sh` → 26 PASS / 0 FAIL (2026-11-11).
- A1 : tag `legacy-v0.3`, dépôt privé `nevenfo/javaquest`, `docs/AUDIT.md`. R1 : `docs/PEDAGOGIE.md` (DOI vérifiés Crossref).

## Décisions actives
- Architecture : voir `plan.md` § Architecture retenue et `docs/AUDIT.md`.
- Sandbox : JVM lancée avec `DETACHED_PROCESS` (pas de console → pas de conhost), env minimal incluant variables de profil (sinon CreateProcess 203), flags JVM `-XX:-UsePerfData -Xms8m -XX:+UseSerialGC`.
- Assets : Kenney Tiny Town + Tiny Dungeon (CC0, 16×16, palette commune) téléchargés dans `.tools/dl/` (non versionné) ; Phaser 3.90.0, CodeMirror 5.65.21 (MIT), JetBrains Mono + Silkscreen (OFL), Kenney Interface Sounds + fonts (CC0).
- Supports ESIEE : `curriculum/originals/` (9 docs, cours « Java EISC4 2026 », Java 25), jamais versionnés ni distribués.

## Blocage actif
Aucun.

## Fichiers / zones utiles
`launcher/Sandbox/` · `tools/test-sandbox.sh` · `tests/sandbox/Attacks.java` · `docs/{AUDIT,SECURITY,PEDAGOGIE}.md` · `.tools/jdk-25` · `.tools/dl/`

## NEXT ACTION
B2.1 — Écrire `runner/` (Java 25) : lit un JSON de sources sur stdin, compile en mémoire via `javax.tools` (`-proc:none`, `--release 25`), renvoie diagnostics réels ; valider par un test runner (code correct / erreur de compilation) exécuté dans `jq-sandbox`.
