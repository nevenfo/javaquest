// Test de bout en bout dans un vrai navigateur (Edge headless via le protocole DevTools, sans dépendance).
// Démarre le serveur (sous-processus Java), crée un profil, joue la première quête et vérifie l'effet dans le monde.
// Usage : node tools/e2e.mjs [--shots dossier]   (outil de développement uniquement)
import { spawn } from 'node:child_process';
import { mkdirSync, rmSync, writeFileSync, readFileSync, readdirSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { tmpdir } from 'node:os';

const ROOT = resolve(import.meta.dirname, '..');
const EDGE = process.env.EDGE || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const shotsDir = process.argv.includes('--shots') ? process.argv[process.argv.indexOf('--shots') + 1] : join(ROOT, 'build', 'e2e');
mkdirSync(shotsDir, { recursive: true });
const data = join(ROOT, 'build', 'e2e-data');
rmSync(data, { recursive: true, force: true });

let pass = 0, fail = 0;
const check = (name, ok, detail = '') => { if (ok) { pass++; console.log('PASS  ' + name); } else { fail++; console.log('FAIL  ' + name + '  ' + String(detail).slice(0, 400)); } };
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

// 1. Serveur
const server = spawn(join(ROOT, '.tools/jdk-25/bin/java.exe'), ['-jar', 'build/server/server.jar', '--app', '.', '--web', 'web', '--content', 'content',
  '--runtime', '.tools/jdk-25', '--sandbox', 'build/sandbox/sandbox.jar', '--runner', 'build/runner/runner.jar', '--data', data, '--watch-stdin', '1'], { cwd: ROOT });
const url = await new Promise((res, rej) => {
  let buf = '';
  server.stdout.on('data', d => { buf += d; const m = buf.match(/JAVAQUEST_READY (\S+)/); if (m) res(m[1]); });
  setTimeout(() => rej(new Error('serveur muet')), 30000);
});

// 2. Navigateur
const profile = join(tmpdir(), 'jq-e2e-' + Date.now());
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
const shot = async (name) => { const r = await cdp('Page.captureScreenshot', { format: 'png' }); writeFileSync(join(shotsDir, name + '.png'), Buffer.from(r.result.data, 'base64')); };
const key = async (code, down = true) => cdp('Input.dispatchKeyEvent', { type: down ? 'rawKeyDown' : 'keyUp', code, key: code.replace('Key', '').toLowerCase(), windowsVirtualKeyCode: { KeyE: 69, KeyW: 87, KeyS: 83, KeyD: 68, KeyA: 65, Enter: 13, Space: 32, Escape: 27 }[code] || 0 });
const press = async (code, ms = 80) => { await key(code, true); await sleep(ms); await key(code, false); };
const truthy = async (expr) => { const r = await cdp('Runtime.evaluate', { expression: expr, awaitPromise: true, returnByValue: true }); return !r.result?.exceptionDetails && !!r.result?.result?.value; };
const waitFor = async (expr, ms = 15000) => { const t = Date.now(); while (Date.now() - t < ms) { if (await truthy(expr)) return true; await sleep(150); } return false; };

try {
  await cdp('Runtime.enable'); await cdp('Page.enable');
  await cdp('Emulation.setFocusEmulationEnabled', { enabled: true });
  await cdp('Page.setWebLifecycleState', { state: 'active' });
  await cdp('Page.navigate', { url });
  check('écran titre affiché', await waitFor(`!!document.querySelector('.title-screen')`));
  await shot('01-titre');
  await js(`document.getElementById('np-name').value = 'Testeuse E2E'; document.getElementById('np-go').click(); true`);
  check('monde chargé après création du profil', await waitFor(`!!(window.JQ.scene && window.JQ.scene.hero)`));
  check('dialogue d\'introduction', await waitFor(`!document.getElementById('dialog').hidden`));
  await shot('02-intro');
  for (let i = 0; i < 3; i++) { await press('Space'); await sleep(150); }
  check('dialogue fermé', await waitFor(`document.getElementById('dialog').hidden`));
  // Déplacement réel au clavier (code physique) : le héros doit bouger.
  const x0 = await js(`window.JQ.scene.hero.x`);
  await key('KeyD', true); await sleep(500); await key('KeyD', false);
  const x1 = await js(`window.JQ.scene.hero.x`);
  check('déplacement clavier (KeyD)', x1 > x0 + 10, `${x0} → ${x1}`);
  // Collision : marcher longtemps vers le haut ne doit pas traverser les maisons/arbres.
  await key('KeyW', true); await sleep(3000); await key('KeyW', false);
  const y1 = await js(`window.JQ.scene.hero.y`);
  check('collisions : le héros reste dans la carte', y1 > 16, y1);
  // Aller devant Ysolde et lui parler.
  await js(`(() => { const s = window.JQ.scene, n = s.npcs.find(n => n.id === 'ysolde'); s.hero.x = n.spr.x; s.hero.y = n.spr.y + 16; s.facing = 'up'; return true; })()`);
  await sleep(300);
  check('invite d\'interaction visible', await waitFor(`!document.getElementById('prompt').hidden && document.getElementById('prompt').textContent.includes('Ysolde')`), await js(`document.getElementById('prompt').textContent`));
  await press('KeyE');
  check('dialogue de quête', await waitFor(`!document.getElementById('dialog').hidden && document.getElementById('dialog-text').textContent.length > 10`));
  await shot('03-dialogue');
  for (let i = 0; i < 3; i++) { await press('Space'); await sleep(120); }
  await js(`document.querySelector('#dialog-choices button')?.click(); true`);
  check('atelier de code ouvert', await waitFor(`!document.getElementById('editor').hidden`));
  // Erreur de compilation réelle
  await js(`window.JQ.workshop.cm.setValue('void main() {\\n    IO.println("Ouvre-toi !")\\n}\\n'); true`);
  await js(`document.getElementById('ed-run').click(); true`);
  check('erreur javac affichée et localisée', await waitFor(`document.getElementById('ed-out').textContent.includes('ligne 2')`, 20000), await js(`document.getElementById('ed-out').textContent`));
  await shot('04-erreur');
  // Code correct : exécution
  await js(`window.JQ.workshop.cm.setValue('void main() {\\n    IO.println("Ouvre-toi !");\\n}\\n'); true`);
  await js(`document.getElementById('ed-out').innerHTML=''; document.getElementById('ed-run').click(); true`);
  check('exécution réelle : sortie', await waitFor(`document.getElementById('ed-out').textContent.includes('Ouvre-toi !')`, 20000), await js(`document.getElementById('ed-out').textContent`));
  await sleep(600);
  await shot('05-execution');
  // D2.3 : débogage visuel (pas-à-pas lié aux lignes) puis aides progressives tracées
  await js(`document.querySelector('#ed-outtabs [data-out="trace"]').click(); true`);
  check('pas-à-pas : étape « affiche » liée à la ligne 2', await waitFor(`[...document.querySelectorAll('#ed-out .trace-row')].some(r => r.textContent.includes('L2'))`), await js(`document.getElementById('ed-out').textContent`));
  await js(`document.querySelector('#ed-out .trace-row').click(); true`);
  check('pas-à-pas : clic → ligne surlignée dans le code', await waitFor(`!!document.querySelector('#editor .line-exec')`));
  await js(`document.querySelector('#ed-infotabs [data-info="hints"]').click(); true`);
  check('indices : aucun révélé d’office', await waitFor(`!!document.getElementById('ed-hint-more')`) && (await js(`document.querySelectorAll('.hint').length`)) === 0);
  await js(`document.getElementById('ed-hint-more').click(); true`);
  check('indices : révélation graduelle tracée (1/3)', await waitFor(`document.querySelectorAll('.hint').length === 1 && window.JQ.session.state.quests['q01-porte'].hintsUsed === 1 && document.getElementById('ed-hintcount').textContent.includes('1/3')`),
    await js(`document.getElementById('ed-hintcount').textContent`));
  await js(`document.querySelector('#ed-outtabs [data-out="console"]').click(); document.querySelector('#ed-infotabs [data-info="brief"]').click(); true`);
  await js(`document.getElementById('ed-check').click(); true`);
  check('quête validée → dialogue de réussite', await waitFor(`!document.getElementById('dialog').hidden && document.getElementById('dialog-text').textContent.includes('porte')`, 20000), await js(`document.getElementById('ed-out').textContent`));
  check('effet dans le monde : porte ouverte', await js(`!!window.JQ.session.state.world.flags['door:sanctuaire']`));
  await shot('06-reussite');
  for (let i = 0; i < 4; i++) { await press('Space'); await sleep(150); }
  check('XP accordée', (await js(`window.JQ.session.state.xp`)) >= 20);
  check('preuve pédagogique enregistrée (guidée)', await js(`window.JQ.session.state.evidence.some(e => e.quest === 'q01-porte' && e.assessment === 'guided' && e.ok)`));
  // Sauvegarde persistée côté serveur puis rechargement
  await sleep(1800);
  await cdp('Page.reload');
  check('rechargement → écran titre avec profil', await waitFor(`!!document.querySelector('[data-play]')`));
  await js(`document.querySelector('[data-play]').click(); true`);
  check('progression restaurée après rechargement', await waitFor(`window.JQ.session && window.JQ.session.state.quests['q01-porte']?.status === 'done' && !!window.JQ.scene?.hero`),
    await js(`JSON.stringify({s: !!window.JQ.session, q: window.JQ.session && window.JQ.session.state.quests, scene: !!window.JQ.scene, fatal: document.getElementById('fatal').textContent, toasts: document.getElementById('toasts').textContent})`));
  await sleep(500);
  await press('KeyG');
  check('grimoire ouvert', await waitFor(`!document.getElementById('modal').hidden && document.getElementById('modal-box').textContent.includes('Grimoire')`));
  await shot('07-grimoire');
  await press('Escape');
  await shot('08-monde');
  // D2.2 : journal (quêtes, chronique, sac) et carte du monde
  await press('KeyJ');
  check('journal : quête accomplie et objet dans le sac', await waitFor(`!document.getElementById('modal').hidden && ['Journal', 'Quête accomplie', 'Sac', 'apprenti'].every(t => document.getElementById('modal-box').textContent.includes(t))`),
    await js(`document.getElementById('modal-box').textContent.slice(0, 300)`));
  await press('Escape');
  await press('KeyM');
  check('carte du monde : position actuelle marquée', await waitFor(`!document.getElementById('modal').hidden && document.getElementById('modal-box').textContent.includes('Carte de Javelune') && !!document.querySelector('.mapnode.here')`));
  await press('Escape');
  // D2.1 : plusieurs profils locaux à sauvegardes séparées
  await js(`document.querySelector('#hud [data-open="menu"]').click(); true`);
  await waitFor(`!!document.getElementById('m-profiles')`);
  await js(`document.getElementById('m-profiles').click(); true`);
  await waitFor(`!!document.getElementById('np-name')`);
  await js(`document.getElementById('np-name').value = 'Second Profil'; document.getElementById('np-go').click(); true`);
  check('second profil : partie vierge, distincte', await waitFor(`window.JQ.session?.state.player.name === 'Second Profil' && !!window.JQ.scene?.hero`) && (await js(`!window.JQ.session.state.quests['q01-porte'] && !window.JQ.session.state.world.flags['door:sanctuaire']`)) === true);
  for (let i = 0; i < 4; i++) { await press('Space'); await sleep(150); }
  await js(`document.querySelector('#hud [data-open="menu"]').click(); true`);
  await waitFor(`!!document.getElementById('m-profiles')`);
  await js(`document.getElementById('m-profiles').click(); true`);
  check('écran titre : deux profils listés', await waitFor(`document.querySelectorAll('[data-play]').length >= 2`));
  await js(`[...document.querySelectorAll('.card.profile')].find(c => c.textContent.includes('Testeuse E2E')).querySelector('[data-play]').click(); true`);
  check('retour au premier profil : progression intacte', await waitFor(`window.JQ.session?.state.player.name === 'Testeuse E2E' && window.JQ.session.state.quests['q01-porte']?.status === 'done' && !!window.JQ.scene?.hero`));
  await press('Escape'); await sleep(300);
  await js(`document.getElementById('modal').hidden || document.querySelector('#modal [data-close]')?.click(); true`);
  // Mode examen : épreuve blanche avec les solutions de référence → toutes les parties réussies.
  await js(`window.confirm = () => true; window.JQ.scene.loadMap('citadelle', { x: 17, y: 23 }); window.JQ.exam.menu(); true`);
  check('pupitre des épreuves', await waitFor(`!!document.querySelector('[data-start="blanc-1"]')`));
  await js(`document.querySelector('[data-start="blanc-1"]').click(); true`);
  check('examen : atelier ouvert avec minuteur', await waitFor(`!document.getElementById('editor').hidden && (document.getElementById('ex-timer')?.textContent || '').includes(':')`));
  check('examen : indices et rappel masqués', await js(`[...document.querySelectorAll('#ed-infotabs button')].filter(b => !b.hidden).map(b => b.dataset.info).join(',') === 'brief,api'`));
  await shot('09-examen');
  const sol = (q) => { const dir = join(ROOT, 'content/quests', q, 'solutions'); const f = readdirSync(dir).filter(n => n.endsWith('.java')); return Object.fromEntries(f.map(n => [n, readFileSync(join(dir, n), 'utf8')])); };
  const answerKey = JSON.parse(readFileSync(join(ROOT, 'content/quests/e01-questions/quest.json'), 'utf8')).answerKey;
  await js(`(() => { const S = window.JQ.session; S.quest('e01-questions').quizAnswers = ${JSON.stringify(answerKey)};
    S.quest('e02-chaudron').drafts = ${JSON.stringify(sol('e02-chaudron'))}; S.quest('e03-runes').drafts = ${JSON.stringify(sol('e03-runes'))};
    S.quest('e04-creatures').drafts = ${JSON.stringify(sol('e04-creatures'))}; return true; })()`);
  await js(`window.JQ.exam.submit(true); true`);
  check('examen : copie corrigée, toutes les parties réussies', await waitFor(`document.getElementById('modal-box').textContent.includes('Résultats') && !document.getElementById('modal-box').textContent.includes('✘')`, 60000), await js(`document.getElementById('modal-box').textContent.slice(0, 600)`));
  check('examen : preuves de type examen enregistrées', await js(`window.JQ.session.state.evidence.filter(e => e.assessment === 'exam' && e.ok).length >= 4`));
  await shot('10-resultats-examen');
  await press('Escape');
  check('aucune exception JavaScript', logs.length === 0, logs.join(' | '));
  // Bouton « Quitter le jeu » : sauvegarde, arrêt du serveur, message final.
  await js(`document.querySelector('#hud [data-open="menu"]').click(); true`);
  await waitFor(`!!document.getElementById('m-quit')`);
  check('menu : bouton Quitter le jeu', await js(`document.getElementById('m-quit').textContent.includes('Quitter')`));
  await js(`document.getElementById('m-quit').click(); true`);
  check('quitter : message final affiché', await waitFor(`!!document.getElementById('bye') && document.body.textContent.includes('fermer cet onglet')`));
  check('quitter : serveur arrêté', await new Promise(r => { if (server.exitCode !== null) return r(true); const t = setTimeout(() => r(false), 8000); server.once('exit', () => { clearTimeout(t); r(true); }); }));
} catch (e) {
  check('exécution du scénario', false, e.stack);
} finally {
  ws.close(); server.stdin.end();
  spawn('taskkill', ['/T', '/F', '/PID', String(edge.pid)]);
  setTimeout(() => { spawn('taskkill', ['/T', '/F', '/PID', String(server.pid)]); try { rmSync(profile, { recursive: true, force: true }); } catch { } }, 2500);
  console.log(`RÉSULTAT : ${pass} PASS, ${fail} FAIL  (captures : ${shotsDir})`);
  setTimeout(() => process.exit(fail ? 1 : 0), 3500);
}
