// Praticabilité des cartes (node --test tests/web/maps.test.mjs) : depuis le spawn de chaque carte, un parcours en largeur
// sur la grille construite doit atteindre tout PNJ et objet (case adjacente), tout portail et tout point
// d'arrivée `at`. L'E2E téléporte via loadMap : seul ce test garantit qu'on peut s'y rendre à pied.
import test from 'node:test';
import assert from 'node:assert/strict';
import { MAPS } from '../../web/js/world/maps.js';

const DIRS = [[1, 0], [-1, 0], [0, 1], [0, -1]];

function grid(def) {
  const g = def.build();
  const blocked = new Set();
  for (const o of def.objects || []) if (o.solid) blocked.add(o.x + ',' + o.y);
  for (const n of def.npcs || []) if (!n.wander) blocked.add(n.x + ',' + n.y);
  const free = (x, y) => x >= 0 && y >= 0 && x < g.w && y < g.h && !g.solid[y * g.w + x] && !blocked.has(x + ',' + y);
  return { g, free };
}

function reach({ g, free }, start) {
  const seen = new Set([start.x + ',' + start.y]);
  const q = [start];
  while (q.length) {
    const { x, y } = q.shift();
    for (const [dx, dy] of DIRS) {
      const nx = x + dx, ny = y + dy, k = nx + ',' + ny;
      if (!seen.has(k) && free(nx, ny)) { seen.add(k); q.push({ x: nx, y: ny }); }
    }
  }
  return seen;
}

const near = (seen, x, y) => seen.has(x + ',' + y) || DIRS.some(([dx, dy]) => seen.has((x + dx) + ',' + (y + dy)));
const cells = (p) => { const r = []; for (let i = 0; i < (p.w || 1); i++) for (let j = 0; j < (p.h || 1); j++) r.push([p.x + i, p.y + j]); return r; };

for (const [id, def] of Object.entries(MAPS)) {
  test(`carte ${id} : tout est atteignable à pied depuis le spawn`, () => {
    const G = grid(def);
    assert.ok(G.free(def.spawn.x, def.spawn.y), `spawn (${def.spawn.x},${def.spawn.y}) praticable`);
    const seen = reach(G, def.spawn);
    for (const n of def.npcs || []) assert.ok(near(seen, n.x, n.y), `PNJ ${n.id} (${n.x},${n.y}) atteignable`);
    for (const o of def.objects || []) assert.ok(near(seen, o.x, o.y), `objet ${o.id} (${o.x},${o.y}) atteignable`);
    for (const p of def.portals || []) assert.ok(cells(p).some(([x, y]) => near(seen, x, y)), `portail vers ${p.to} (${p.x},${p.y}) atteignable`);
  });
}

test('points d\'arrivée des portails praticables et reliés au spawn de la carte cible', () => {
  for (const [id, def] of Object.entries(MAPS)) for (const p of def.portals || []) {
    const target = MAPS[p.to];
    assert.ok(target, `${id} → carte inconnue ${p.to}`);
    const G = grid(target);
    assert.ok(G.free(p.at.x, p.at.y), `${id} → ${p.to} : arrivée (${p.at.x},${p.at.y}) praticable`);
    assert.ok(reach(G, target.spawn).has(p.at.x + ',' + p.at.y), `${id} → ${p.to} : arrivée reliée au spawn`);
  }
});
