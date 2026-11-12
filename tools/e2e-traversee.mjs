// Test de bout en bout : traversée complète du jeu (village -> citadelle) avec les solutions de référence.
// Démarre le serveur (sous-processus Java) et Edge headless (protocole DevTools), crée un profil, puis résout la chaîne
// principale (et ses prérequis) dans l'ordre topologique, par le chemin du client (Workshop.openQuest + check -> /api/check -> onPass).
// Vérifie à chaque quête : réussite, état (quête done, flags/portes des effets), puis portails de région et accès à la citadelle.
// Usage : node tools/e2e-traversee.mjs   (outil de développement uniquement ; lit content/ pour les solutions)
import { spawn } from 'node:child_process';
import { mkdirSync, rmSync, readFileSync, readdirSync, existsSync } from 'node:fs';
import { join, resolve, basename } from 'node:path';
import { tmpdir } from 'node:os';

const ROOT = resolve(import.meta.dirname, '..');
const EDGE = process.env.EDGE || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const data = join(ROOT, 'build', 'e2e-trav-data');
rmSync(data, { recursive: true, force: true });
const t0 = Date.now();

let pass = 0, fail = 0;
const check = (name, ok, detail = '') => { if (ok) { pass++; console.log('PASS  ' + name); } else { fail++; console.log('FAIL  ' + name + '  ' + String(detail).slice(0, 500)); } };
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

// ---------- Contenu côté disque ----------
const QDIR = join(ROOT, 'content/quests');
const quests = readdirSync(QDIR).sort().map(n => ({ dir: n, ...JSON.parse(readFileSync(join(QDIR, n, 'quest.json'), 'utf8')) }));
const byId = new Map(quests.map(q => [q.id, q]));
const order = [], seen = new Set();
const visit = (q, stack = []) => {
  if (seen.has(q.id)) return;
  if (stack.includes(q.id)) throw new Error('cycle requires : ' + [...stack, q.id].join(' > '));
  for (const r of q.requires || []) { const p = byId.get(r); if (!p) throw new Error(`requires inconnu : ${q.id} -> ${r}`); visit(p, [...stack, q.id]); }
  seen.add(q.id); order.push(q);
};
// Ordre de jeu réel : régions dans l'ordre des portails (village, forêt, mines, artisans, tour, bibliothèque), prérequis d'abord.
const RANK = ['village', 'foret', 'mines', 'artisans', 'tour', 'bibliotheque'];
quests.filter(q => q.main).sort((a, b) => RANK.indexOf(a.region) - RANK.indexOf(b.region)).forEach(q => visit(q));
const nMain = order.length;
// Donjon optionnel de la crypte (après la chaîne principale) : quêtes non principales de la région `crypte`, prérequis d'abord.
quests.filter(q => q.region === 'crypte').forEach(q => visit(q));
const solutionFiles = (q) => {
  const variant = (q.solutions || [])[0]; if (!variant) return null;
  return Object.fromEntries(variant.map(p => [basename(p), readFileSync(join(QDIR, q.dir, p), 'utf8')]));
};

// ---------- Serveur ----------
const server = spawn(join(ROOT, '.tools/jdk-25/bin/java.exe'), ['-jar', 'build/server/server.jar', '--app', '.', '--web', 'web', '--content', 'content',
  '--runtime', '.tools/jdk-25', '--sandbox', 'launcher/Sandbox/bin/Release/jq-sandbox.exe', '--runner', 'build/runner/runner.jar', '--data', data, '--watch-stdin', '1'], { cwd: ROOT });
const url = await new Promise((res, rej) => {
  let buf = '';
  server.stdout.on('data', d => { buf += d; const m = buf.match(/JAVAQUEST_READY (\S+)/); if (m) res(m[1]); });
  setTimeout(() => rej(new Error('serveur muet')), 30000);
});

// ---------- Navigateur ----------
const profile = join(tmpdir(), 'jq-e2e-trav-' + Date.now());
const PORT = 9300 + Math.floor(Math.random() * 600);
const edge = spawn(EDGE, ['--headless=new', `--remote-debugging-port=${PORT}`, `--user-data-dir=${profile}`, '--window-size=1600,900', '--mute-audio', '--no-first-run', 'about:blank']);
let wsUrl;
for (let i = 0; i < 50 && !wsUrl; i++) {
  await sleep(200);
  try { const t = await (await fetch(`http://127.0.0.1:${PORT}/json`)).json(); wsUrl = t.find(x => x.type === 'page')?.webSocketDebuggerUrl; } catch { }
}
const ws = new WebSocket(wsUrl);
await new Promise(r => ws.onopen = r);
let id = 0; const pending = new Map(); const logs = [];
ws.onmessage = (m) => {
  const msg = JSON.parse(m.data);
  if (msg.id && pending.has(msg.id)) { pending.get(msg.id)(msg); pending.delete(msg.id); }
  if (msg.method === 'Runtime.exceptionThrown') logs.push('EXCEPTION ' + msg.params.exceptionDetails.exception?.description);
  if (msg.method === 'Runtime.consoleAPICalled' && msg.params.type === 'error') logs.push('console.error ' + msg.params.args.map(a => a.value || a.description).join(' '));
};
const cdp = (method, params = {}) => new Promise(r => { const i = ++id; pending.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
const js = async (expr) => { const r = await cdp('Runtime.evaluate', { expression: expr, awaitPromise: true, returnByValue: true }); return r.result?.result?.value ?? r.result?.exceptionDetails?.exception?.description; };
const key = async (code, down = true) => cdp('Input.dispatchKeyEvent', { type: down ? 'rawKeyDown' : 'keyUp', code, key: code === 'Space' ? ' ' : code, windowsVirtualKeyCode: { Space: 32, Enter: 13 }[code] || 0 });
const press = async (code, ms = 60) => { await key(code, true); await sleep(ms); await key(code, false); };
const truthy = async (expr) => { const r = await cdp('Runtime.evaluate', { expression: expr, awaitPromise: true, returnByValue: true }); return !r.result?.exceptionDetails && !!r.result?.result?.value; };
const waitFor = async (expr, ms = 15000) => { const t = Date.now(); while (Date.now() - t < ms) { if (await truthy(expr)) return true; await sleep(120); } return false; };
const dialogOpen = `!document.getElementById('dialog').hidden`;
const closeDialogs = async () => { for (let i = 0; i < 25 && await truthy(dialogOpen); i++) { await press('Space'); await sleep(110); } };

const mapOf = () => js(`window.JQ.scene.mapId`);
const goPortal = async (from, to) => {
  await js(`window.JQ.scene.loadMap('${from}'); true`);
  await waitFor(`window.JQ.scene.mapId === '${from}'`);
  await sleep(700);
  await js(`(async () => { const { MAPS } = await import('/js/world/maps.js'); const p = MAPS['${from}'].portals.find(p => p.to === '${to}'); window.JQ.scene.usePortal(p); return true; })()`);
};

try {
  await cdp('Runtime.enable'); await cdp('Page.enable');
  await cdp('Emulation.setFocusEmulationEnabled', { enabled: true });
  await cdp('Page.setWebLifecycleState', { state: 'active' });
  await cdp('Page.navigate', { url });
  check('écran titre affiché', await waitFor(`!!document.querySelector('.title-screen')`));
  await js(`document.getElementById('np-name').value = 'Traversée E2E'; document.getElementById('np-go').click(); true`);
  check('monde chargé après création du profil', await waitFor(`!!(window.JQ.scene && window.JQ.scene.hero)`));
  await waitFor(dialogOpen); await closeDialogs();
  const clientIds = await js(`window.JQ.quests.map(q => q.id).join(',')`);
  check('contenu client = contenu disque', order.every(q => clientIds.split(',').includes(q.id)), 'quêtes manquantes côté client');
  console.log(`Chaîne à traverser : ${order.length} quêtes (${nMain} principales + prérequis, puis ${order.length - nMain} de la crypte)`);

  // Portails initialement verrouillés
  for (const [from, to] of [['village', 'foret'], ['village', 'mines'], ['village', 'artisans'], ['village', 'bibliotheque'], ['artisans', 'tour'], ['mines', 'crypte']]) {
    await goPortal(from, to); await sleep(500);
    check(`portail verrouillé au départ : ${from} → ${to}`, (await mapOf()) === from, await mapOf());
  }
  check('sanctuaire fermé au départ', !(await js(`!!window.JQ.session.state.world.flags['door:sanctuaire']`)));

  // Refus d'une réussite gratuite : le code de départ de q01-porte ne valide pas
  await js(`window.JQ.scene.loadMap('village'); window.JQ.workshop.openQuest(window.JQ.byId.get('q01-porte')); true`);
  await waitFor(`!document.getElementById('editor').hidden`);
  await js(`document.getElementById('ed-check').click(); true`);
  await waitFor(`!document.getElementById('ed-check').disabled && /\\S/.test(document.getElementById('ed-out').textContent)`, 30000);
  check('code de départ refusé (pas de réussite gratuite)', await js(`window.JQ.session.state.quests['q01-porte']?.status !== 'done'`));
  await js(`window.JQ.workshop.close(); true`);

  // ---------- Traversée ----------
  const entered = new Set(['village']);
  const travel = { foret: ['village', 'foret'], mines: ['village', 'mines'], artisans: ['village', 'artisans'], tour: ['artisans', 'tour'], crypte: ['mines', 'crypte'], bibliotheque: ['village', 'bibliotheque'] };
  let doneCount = 0, aborted = false;
  for (const q of order) {
    if (aborted) break;
    if (!entered.has(q.region)) {
      entered.add(q.region);
      const [from, to] = travel[q.region] || [];
      if (from) {
        await goPortal(from, to);
        check(`portail ouvert : ${from} → ${to}`, await waitFor(`window.JQ.scene.mapId === '${to}'`, 25000), await js(`JSON.stringify({m: window.JQ.scene.mapId, tr: window.JQ.scene.travelling, fr: window.JQ.scene.frozen, toast: document.getElementById('toasts').textContent})`));
      } else check(`région connue : ${q.region}`, false, 'pas de portail défini dans le test');
    }
    await js(`window.JQ.scene.loadMap('${q.region}'); true`);
    const avail = await js(`window.JQ.byId.get('${q.id}').requires.every(r => window.JQ.session.state.quests[r]?.status === 'done')`);
    if (!avail) { check(`${q.id} : disponible (prérequis faits)`, false); aborted = true; break; }
    // Préparer la saisie comme le ferait le joueur
    const prep = { sol: null, answers: null, prediction: null };
    if (q.kind === 'quiz') prep.answers = q.answerKey;
    else if (q.kind === 'predict') prep.prediction = q.expected;
    else prep.sol = solutionFiles(q);
    if (q.kind === 'code' || q.kind === 'fix') { if (!prep.sol) { check(`${q.id} : solution de référence présente`, false); aborted = true; break; } }
    await js(`(() => { const S = window.JQ.session, qs = S.quest('${q.id}');
      const p = ${JSON.stringify(prep)};
      if (p.sol) qs.drafts = p.sol; if (p.answers) qs.quizAnswers = p.answers; if (p.prediction != null) qs.prediction = p.prediction;
      window.JQ.workshop.openQuest(window.JQ.byId.get('${q.id}')); return true; })()`);
    if (!(await waitFor(`!document.getElementById('editor').hidden`))) { check(`${q.id} : atelier ouvert`, false); aborted = true; break; }
    await js(`document.getElementById('ed-check').click(); true`);
    const ok = await waitFor(`window.JQ.session.state.quests['${q.id}']?.status === 'done'`, 90000);
    if (!ok) { check(`${q.id} (${q.region}/${q.kind}) réussie`, false, await js(`document.getElementById('ed-out').textContent`)); aborted = true; break; }
    await closeDialogs();
    await js(`window.JQ.workshop.open && window.JQ.workshop.close(); true`);
    doneCount++;
    // Effets d'état
    let effOk = true, effDetail = '';
    for (const eff of q.effects?.onPass || []) {
      const [kind, a] = eff.split(':');
      if (kind === 'flag' || kind === 'door') {
        const f = kind === 'door' ? 'door:' + a : a;
        if (!(await js(`!!window.JQ.session.state.world.flags['${f}']`))) { effOk = false; effDetail += ' flag manquant ' + f; }
      }
    }
    const n = await js(`Object.values(window.JQ.session.state.quests).filter(s => s.status === 'done').length`);
    check(`${q.id} (${q.region}/${q.kind}) réussie, état à jour`, effOk && n === doneCount, `done=${n} attendu=${doneCount}${effDetail}`);
  }
  const mainDone = await js(`window.JQ.quests.filter(q => q.main).every(q => window.JQ.session.state.quests[q.id]?.status === 'done')`);
  check('chaîne principale entièrement accomplie', !aborted && mainDone === true);

  // Preuves, portes, citadelle
  const flags = await js(`JSON.stringify(window.JQ.session.state.world.flags)`);
  for (const f of ['door:sanctuaire', 'f-foret', 'f-mines', 'f-forge', 'f-tour', 'f-crypte']) check(`flag ${f} posé`, !!JSON.parse(flags || '{}')[f], flags);
  check('preuves pédagogiques enregistrées', await js(`window.JQ.session.state.evidence.length >= ${Math.max(1, doneCount)}`));
  check('XP accordée', (await js(`window.JQ.session.state.xp`)) > 0);
  await js(`window.JQ.scene.loadMap('village'); window.JQ.exam.enter(); true`);
  check('citadelle accessible (sanctuaire ouvert)', await waitFor(`window.JQ.scene.mapId === 'citadelle'`, 6000), await mapOf());
  check('pupitre des épreuves présent dans la citadelle', await js(`!!window.JQ.scene.def.objects.find(o => o.action === 'exam')`));
  // Sauvegarde durable
  await js(`window.JQ.session.dirty = true; window.JQ.session.flush().then(() => true)`);
  const savedDone = await js(`(async () => { const { api } = await import('/js/api.js'); const s = await api.load(window.JQ.session.profileId); return Object.values(s.quests).filter(q => q.status === 'done').length; })()`);
  check('sauvegarde serveur cohérente', savedDone === doneCount, `${savedDone} vs ${doneCount}`);
  check('aucune exception JavaScript', logs.length === 0, logs.join(' | '));
} catch (e) {
  check('exécution du scénario', false, e.stack);
} finally {
  ws.close(); server.stdin.end();
  spawn('taskkill', ['/T', '/F', '/PID', String(edge.pid)]);
  setTimeout(() => { spawn('taskkill', ['/T', '/F', '/PID', String(server.pid)]); try { rmSync(profile, { recursive: true, force: true }); } catch { } }, 2500);
  console.log(`RÉSULTAT : ${pass} PASS, ${fail} FAIL  (${Math.round((Date.now() - t0) / 1000)} s)`);
  setTimeout(() => process.exit(fail ? 1 : 0), 3500);
}
