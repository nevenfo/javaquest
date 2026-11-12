// Tests de l'état de partie et de la sauvegarde (node --test tests/web). Serveur simulé par un faux fetch.
import test, { mock } from 'node:test';
import assert from 'node:assert/strict';
import { SCHEMA, newGame, migrate, Session } from '../../web/js/state.js';

// Faux serveur local : un fichier par profil, écriture « atomique » (copie profonde).
function fakeServer({ fail = 0 } = {}) {
  const files = new Map(), calls = [];
  globalThis.fetch = async (path, opts = {}) => {
    const profile = decodeURIComponent((path.match(/profile=([^&]+)/) || [])[1] || '');
    calls.push({ method: opts.method, path, token: opts.headers?.['X-JQ-Token'] });
    if (opts.method === 'PUT') {
      if (fail > 0) { fail--; return { ok: false, status: 500, json: async () => ({ error: 'disque plein' }) }; }
      files.set(profile, JSON.parse(opts.body));
      return { ok: true, status: 200, json: async () => ({ ok: true }) };
    }
    return { ok: true, status: 200, json: async () => structuredClone(files.get(profile)) };
  };
  globalThis.document = { events: [], dispatchEvent(e) { this.events.push(e); } };
  globalThis.CustomEvent ||= class extends Event { constructor(t, o) { super(t); this.detail = o?.detail; } };
  return { files, calls };
}
const flushMicrotasks = () => new Promise(r => setImmediate(r));

test('newGame : partie vierge au schéma courant, objets indépendants', () => {
  const a = newGame('Ada'), b = newGame('Bob');
  assert.equal(a.schema, SCHEMA);
  assert.equal(a.player.name, 'Ada');
  assert.equal(a.world.map, 'village');
  assert.deepEqual([a.quests, a.evidence, a.inventory, a.xp], [{}, [], [], 0]);
  a.world.flags.x = true; a.inventory.push('clé');
  assert.deepEqual([b.world.flags, b.inventory], [{}, []], 'deux parties ne partagent aucun état');
});

test('migrate : complète une ancienne sauvegarde sans perdre la progression', () => {
  const old = { schema: 1, player: { name: 'Ada' }, world: { map: 'foret', flags: { 'f-foret': true } }, quests: { q01: { status: 'done' } }, xp: 40, settings: { volume: 0.2 } };
  const s = migrate(old);
  assert.equal(s.world.map, 'foret');
  assert.equal(s.world.facing, 'down', 'champ manquant restauré');
  assert.deepEqual(s.world.flags, { 'f-foret': true });
  assert.equal(s.quests.q01.status, 'done');
  assert.equal(s.xp, 40);
  assert.deepEqual(s.settings, { volume: 0.2, speed: 1 });
  assert.deepEqual(s.stats, { playSeconds: 0, runs: 0, checks: 0 });
  assert.deepEqual(s.evidence, []);
  assert.equal(migrate({}).player.name, 'Aventurier');
});

test('migrate : refuse une sauvegarde illisible ou plus récente', () => {
  assert.throws(() => migrate(null), /illisible/);
  assert.throws(() => migrate('texte'), /illisible/);
  assert.throws(() => migrate({ schema: SCHEMA + 1 }), /plus récente/);
});

test('Session : quêtes et drapeaux, notification des abonnés', (t) => {
  t.mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  fakeServer();
  const s = new Session('p1', newGame('Ada'));
  const q = s.quest('q01');
  assert.deepEqual(q, { status: 'available', attempts: 0, checks: 0, hintsUsed: 0, solutionShown: false, drafts: null });
  assert.equal(s.quest('q01'), q, 'même objet au second appel');
  let seen = 0; const off = s.onChange(() => seen++);
  assert.equal(s.flag('f-foret'), false);
  s.setFlag('f-foret');
  assert.equal(s.flag('f-foret'), true);
  assert.equal(seen, 1);
  off(); s.setFlag('autre');
  assert.equal(seen, 1, 'désabonné');
  clearInterval(s.tick); clearTimeout(s.timer);
});

test('Session : sauvegarde différée regroupée puis rechargement fidèle', async (t) => {
  t.mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  const srv = fakeServer();
  const s = new Session('p1', newGame('Ada'));
  s.state.xp = 10; s.touch(); s.setFlag('door:sanctuaire'); s.quest('q01').status = 'done'; s.touch();
  assert.equal(srv.calls.length, 0, 'rien avant le délai');
  t.mock.timers.tick(1000); await flushMicrotasks();
  assert.equal(srv.calls.filter(c => c.method === 'PUT').length, 1, 'trois modifications → une écriture');
  assert.equal(s.dirty, false);
  const reloaded = migrate(srv.files.get('p1'));
  assert.equal(reloaded.xp, 10);
  assert.equal(reloaded.quests.q01.status, 'done');
  assert.equal(reloaded.world.flags['door:sanctuaire'], true);
  clearInterval(s.tick);
});

test('Session : échec d\'écriture signalé, état gardé « sale » et réécrit ensuite', async (t) => {
  t.mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  const srv = fakeServer({ fail: 1 });
  const s = new Session('p1', newGame('Ada'));
  s.state.xp = 5; s.touch();
  t.mock.timers.tick(1000); await flushMicrotasks();
  assert.equal(s.dirty, true);
  assert.match(s.lastError.message, /disque plein/);
  assert.equal(document.events.at(-1).type, 'jq-save-error');
  assert.equal(srv.files.has('p1'), false);
  await s.flush();
  assert.equal(s.lastError, null);
  assert.equal(srv.files.get('p1').xp, 5);
  clearInterval(s.tick);
});

test('Session : chronomètre de jeu toutes les 15 s', async (t) => {
  t.mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  const srv = fakeServer();
  const s = new Session('p1', newGame('Ada'));
  t.mock.timers.tick(30000); await flushMicrotasks();
  assert.equal(s.state.stats.playSeconds, 30);
  t.mock.timers.tick(1000); await flushMicrotasks();
  assert.equal(srv.files.get('p1').stats.playSeconds, 30);
  clearInterval(s.tick);
});

test('Profils : deux profils ont des sauvegardes séparées ; dispose arrête l\'ancienne session', async (t) => {
  t.mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  const srv = fakeServer();
  const a = new Session('ada', newGame('Ada'));
  a.state.xp = 30; a.touch();
  await a.dispose();
  assert.equal(srv.files.get('ada').xp, 30, 'dispose écrit l\'état en attente');
  const b = new Session('bob', newGame('Bob'));
  b.state.xp = 7; b.touch();
  const writes = srv.calls.length;
  t.mock.timers.tick(60000); await flushMicrotasks();
  assert.equal(a.state.stats.playSeconds, 0, 'session close : plus de chronomètre');
  assert.ok(srv.calls.slice(writes).every(c => c.path.includes('profile=bob')), 'aucune écriture tardive sur l\'ancien profil');
  assert.equal(srv.files.get('ada').xp, 30);
  assert.equal(srv.files.get('bob').xp, 7);
  assert.equal(srv.files.get('bob').player.name, 'Bob');
  await b.dispose();
});

test('flushOnExit : écriture keepalive seulement si modifié', () => {
  mock.timers.enable({ apis: ['setInterval', 'setTimeout'] });
  const srv = fakeServer();
  const s = new Session('p1', newGame('Ada'));
  s.flushOnExit();
  assert.equal(srv.calls.length, 0);
  s.state.xp = 1; s.touch(); s.flushOnExit();
  assert.equal(srv.calls.length, 1);
  assert.equal(s.dirty, false);
  clearInterval(s.tick); clearTimeout(s.timer);
  mock.timers.reset();
});
