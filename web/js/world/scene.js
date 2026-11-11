// Scène Phaser du monde : carte tuilée, héros, PNJ, objets, portes, portails, effets du code Java.
import { MAPS, NPCS } from './maps.js';
import { TILE } from './builder.js';
import { play } from '../sound.js';

const TS = 16;
const KEYS = { up: ['KeyW', 'ArrowUp'], down: ['KeyS', 'ArrowDown'], left: ['KeyA', 'ArrowLeft'], right: ['KeyD', 'ArrowRight'] };

/** Clavier par code physique (ZQSD en AZERTY = WASD en QWERTY), ignoré pendant la saisie. */
class Input {
  constructor() {
    this.down = new Set();
    this.enabled = true;
    addEventListener('keydown', (e) => { if (!this.typing(e)) this.down.add(e.code); });
    addEventListener('keyup', (e) => this.down.delete(e.code));
    addEventListener('blur', () => this.down.clear());
  }
  typing(e) { const t = e.target; return t && (t.isContentEditable || /INPUT|TEXTAREA|SELECT/.test(t.tagName) || t.closest?.('.CodeMirror')); }
  is(dir) { return this.enabled && KEYS[dir].some(k => this.down.has(k)); }
}

export class WorldScene extends Phaser.Scene {
  constructor(hooks) {
    super('world');
    this.hooks = hooks;          // { session, onInteract, onEnterMap, onBlocked, isDone(questId) }
    this.input2 = new Input();
    this.bubbles = [];
    this.actors = new Map();     // acteurs temporaires d'une scène de code
  }

  preload() {
    this.load.spritesheet('town', 'assets/tiles/town.png', { frameWidth: 16, frameHeight: 16 });
    this.load.spritesheet('dungeon', 'assets/tiles/dungeon.png', { frameWidth: 16, frameHeight: 16 });
  }

  create() {
    this.cameras.main.setRoundPixels(true);
    this.scale.on('resize', () => this.layout());
    this.bubbleLayer = document.createElement('div');
    this.bubbleLayer.style.cssText = 'position:fixed;inset:0;pointer-events:none;z-index:12';
    document.body.appendChild(this.bubbleLayer);
    this.hooks.ready?.(this);
  }

  /** gid → (clé de texture, frame). */
  frameOf(gid) { return gid > 132 ? ['dungeon', gid - 133] : ['town', gid - 1]; }

  loadMap(id, at) {
    const def = MAPS[id];
    const s = this.hooks.session.state;
    this.children.removeAll(true);
    this.actors.clear();
    this.mapId = id; this.def = def;
    this.geo = def.build();
    const { w, h } = this.geo;
    // Rendu : une image par tuile non vide (cartes ≤ 50×36 : performance largement suffisante).
    this.add.rectangle(0, 0, w * TS, h * TS, 0x2b2540).setOrigin(0).setDepth(-10);
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
      const g = this.geo.ground[y * w + x];
      if (g) { const [k, f] = this.frameOf(g); this.add.image(x * TS, y * TS, k, f).setOrigin(0).setDepth(-5); }
      const d = this.geo.deco[y * w + x];
      if (d) { const [k, f] = this.frameOf(d); this.add.image(x * TS, y * TS, k, f).setOrigin(0).setDepth(y * TS + TS - 1); }
    }
    // Portes de maisons
    this.doorSprites = new Map();
    for (const door of this.geo.doors) {
      const open = !!s.world.flags['door:' + door.id];
      const [k, f] = this.frameOf(open ? door.open : door.closed);
      const img = this.add.image(door.x * TS, door.y * TS, k, f).setOrigin(0).setDepth(door.y * TS + TS);
      this.doorSprites.set(door.id, { img, door });
    }
    // Objets
    this.objects = [];
    for (const o of def.objects || []) this.addObject(o);
    for (const o of this.hooks.extraObjects?.(id) || []) this.addObject(o);
    // PNJ
    this.npcs = [];
    for (const n of def.npcs) {
      const info = NPCS[n.id];
      const spr = this.add.image(n.x * TS + 8, n.y * TS + 15, 'dungeon', info.sprite).setOrigin(0.5, 1);
      spr.setDepth(spr.y);
      const npc = { ...n, info, spr, home: { x: n.x, y: n.y }, t: Math.random() * 3 };
      this.tweens.add({ targets: spr, scaleY: 0.94, duration: 700 + Math.random() * 300, yoyo: true, repeat: -1, ease: 'Sine.easeInOut' });
      npc.marker = this.add.text(spr.x, spr.y - 19, '', { fontFamily: 'Silkscreen', fontSize: '10px', color: '#ffd36e', stroke: '#2a1d16', strokeThickness: 3, resolution: 4 }).setOrigin(0.5, 1).setDepth(9000);
      this.npcs.push(npc);
    }
    // Héros
    const spawn = at || (s.world.map === id && s.world.x != null ? { x: s.world.x, y: s.world.y } : def.spawn);
    const avatar = s.player.avatarSprite ?? 85;
    this.hero = this.add.image(spawn.x * TS + 8, spawn.y * TS + 15, 'dungeon', avatar).setOrigin(0.5, 1);
    this.hero.setDepth(this.hero.y);
    this.heroShadow = this.add.ellipse(this.hero.x, this.hero.y, 10, 4, 0x000000, 0.25).setDepth(-1);
    this.facing = s.world.facing || 'down';
    this.stepT = 0;
    const cam = this.cameras.main;
    cam.setBounds(0, 0, w * TS, h * TS);
    cam.startFollow(this.hero, true, 0.15, 0.15);
    this.layout();
    s.world.map = id;
    this.saveHeroPos();
    this.refreshMarkers();
    this.hooks.onEnterMap?.(id, def);
    cam.fadeIn(250, 20, 18, 30);
  }

  addObject(o) {
    const s = this.hooks.session.state;
    if (o.when && !s.world.flags[o.when]) return;
    if (o.unless && s.world.flags[o.unless]) return;
    const tile = (o.tileWhen && s.world.flags[o.tileWhen.flag]) ? o.tileWhen.tile : o.tile;
    const [k, f] = this.frameOf(tile);
    const img = this.add.image(o.x * TS, o.y * TS, k, f).setOrigin(0).setDepth(o.y * TS + TS);
    if (o.glow) this.tweens.add({ targets: img, alpha: 0.7, duration: 900, yoyo: true, repeat: -1 });
    this.objects.push({ ...o, img });
  }

  layout() {
    const cam = this.cameras.main;
    const W = this.scale.width, H = this.scale.height;
    const panel = this.hooks.panelWidth?.() || 0;
    cam.setViewport(0, 0, Math.max(200, W - panel), H);
    const zoom = Math.max(2, Math.min(4, Math.round(Math.min(W - panel, H * 1.6) / (TS * 22))));
    cam.setZoom(zoom);
  }

  // ---------- Collisions ----------
  solidAt(px, py) {
    const x = Math.floor(px / TS), y = Math.floor(py / TS);
    const { w, h, solid } = this.geo;
    if (x < 0 || y < 0 || x >= w || y >= h) return !this.portalAt(px, py);
    if (solid[y * w + x]) {
      for (const [id, d] of this.doorSprites) if (d.door.x === x && d.door.y === y && this.hooks.session.state.world.flags['door:' + id]) return false;
      return true;
    }
    for (const o of this.objects) if (o.solid && o.x === x && o.y === y) return true;
    for (const n of this.npcs) if (Math.abs(n.spr.x - px) < 7 && Math.abs(n.spr.y - 3 - py) < 5) return true;
    for (const a of this.actors.values()) if (a.solid && Math.abs(a.spr.x - px) < 7 && Math.abs(a.spr.y - 3 - py) < 5) return true;
    return false;
  }

  blocked(x, y) { // boîte aux pieds du héros (10×5)
    return this.solidAt(x - 5, y - 5) || this.solidAt(x + 4, y - 5) || this.solidAt(x - 5, y - 1) || this.solidAt(x + 4, y - 1);
  }

  portalAt(px, py) {
    const x = Math.floor(px / TS), y = Math.floor(py / TS);
    return (this.def.portals || []).find(p => x >= p.x && x < p.x + (p.w || 1) && y >= p.y && y < p.y + (p.h || 1));
  }

  // ---------- Boucle ----------
  update(time, delta) {
    if (!this.hero) return;
    const dt = Math.min(delta, 50) / 1000;
    this.updateNpcs(dt);
    this.updateBubbles();
    if (this.frozen) return;
    let vx = 0, vy = 0;
    if (this.input2.is('left')) vx -= 1;
    if (this.input2.is('right')) vx += 1;
    if (this.input2.is('up')) vy -= 1;
    if (this.input2.is('down')) vy += 1;
    if (vx || vy) {
      const len = Math.hypot(vx, vy), speed = 72;
      const nx = this.hero.x + (vx / len) * speed * dt, ny = this.hero.y + (vy / len) * speed * dt;
      if (!this.blocked(nx, this.hero.y)) this.hero.x = nx;
      if (!this.blocked(this.hero.x, ny)) this.hero.y = ny;
      this.facing = Math.abs(vx) > Math.abs(vy) ? (vx < 0 ? 'left' : 'right') : (vy < 0 ? 'up' : 'down');
      if (vx) this.hero.setFlipX(vx < 0);
      this.stepT += dt;
      this.hero.setScale(1, 1 - 0.06 * Math.abs(Math.sin(this.stepT * 14)));
      if (this.stepT > 0.32) { this.stepT = 0; play('step'); }
      this.hero.setDepth(this.hero.y);
      this.moved = true;
      const p = this.portalAt(this.hero.x, this.hero.y - 3);
      if (p) this.usePortal(p);
    } else {
      this.hero.setScale(1, 1);
      if (this.moved) { this.moved = false; this.saveHeroPos(); }
    }
    this.heroShadow.setPosition(this.hero.x, this.hero.y);
    this.updatePrompt();
  }

  updateNpcs(dt) {
    for (const n of this.npcs) {
      n.marker.setPosition(n.spr.x, n.spr.y - 16);
      if (!n.wander || this.frozen) continue;
      n.t -= dt;
      if (n.t <= 0) {
        n.t = 2 + Math.random() * 3;
        const tx = n.home.x + Math.round((Math.random() - 0.5) * 4), ty = n.home.y + Math.round((Math.random() - 0.5) * 2);
        const px = tx * TS + 8, py = ty * TS + 15;
        if (!this.solidAt(px, py - 3) && Phaser.Math.Distance.Between(px, py, this.hero.x, this.hero.y) > 18) {
          n.spr.setFlipX(px < n.spr.x);
          this.tweens.add({ targets: n.spr, x: px, y: py, duration: 1200, onUpdate: () => n.spr.setDepth(n.spr.y) });
        }
      }
    }
  }

  saveHeroPos() {
    const w = this.hooks.session.state.world;
    w.x = Math.floor(this.hero.x / TS); w.y = Math.floor((this.hero.y - 3) / TS); w.facing = this.facing;
    this.hooks.session.touch();
  }

  /** Ce que le héros peut activer devant lui (PNJ, objet, porte). */
  target() {
    const fx = { left: -1, right: 1, up: 0, down: 0 }[this.facing], fy = { up: -1, down: 1, left: 0, right: 0 }[this.facing];
    const px = this.hero.x + fx * 12, py = this.hero.y - 4 + fy * 12;
    let best = null, bd = 22;
    for (const n of this.npcs) {
      const d = Phaser.Math.Distance.Between(px, py, n.spr.x, n.spr.y - 6);
      if (d < bd) { bd = d; best = { kind: 'npc', npc: n, label: `Parler à ${n.info.name}` }; }
    }
    for (const o of this.objects) {
      if (!o.label) continue;
      const d = Phaser.Math.Distance.Between(px, py, o.x * TS + 8, o.y * TS + 8);
      if (d < bd) { bd = d; best = { kind: 'object', obj: o, label: o.label }; }
    }
    for (const [id, { door }] of this.doorSprites) {
      const d = Phaser.Math.Distance.Between(px, py, door.x * TS + 8, door.y * TS + 8);
      if (d < bd) { bd = d; best = { kind: 'door', id, door, label: this.hooks.doorLabel?.(id) || 'Examiner la porte' }; }
    }
    return best;
  }

  updatePrompt() {
    const t = this.frozen ? null : this.target();
    const el = document.getElementById('prompt');
    if (t) { el.textContent = `E · ${t.label}`; el.hidden = false; } else el.hidden = true;
    this.currentTarget = t;
  }

  interact() {
    if (this.frozen || !this.currentTarget) return;
    const t = this.currentTarget;
    if (t.kind === 'npc') {
      const n = t.npc;
      n.spr.setFlipX(this.hero.x < n.spr.x);
    }
    this.hooks.onInteract?.(t);
  }

  usePortal(p) {
    if (this.travelling) return;
    if (p.requires && !this.hooks.isDone(p.requires)) {
      this.hero.y -= 0; // repousse le héros hors du portail
      const back = { left: [6, 0], right: [-6, 0], up: [0, 6], down: [0, -6] }[this.facing];
      this.hero.x += back[0]; this.hero.y += back[1];
      this.hooks.onBlocked?.(p.locked || 'Ce passage est encore fermé.');
      return;
    }
    this.travelling = true;
    play('creak');
    this.cameras.main.fadeOut(220, 20, 18, 30);
    this.time.delayedCall(240, () => { this.travelling = false; this.loadMap(p.to, p.at); });
  }

  freeze(on) { this.frozen = on; this.input2.enabled = !on; if (on) document.getElementById('prompt').hidden = true; }

  refreshMarkers() {
    for (const n of this.npcs || []) n.marker.setText(this.hooks.npcMarker?.(n.id) || '');
  }

  // ---------- Effets du monde ----------
  openDoor(id, animate = true) {
    const d = this.doorSprites?.get(id);
    if (!d) return;
    const [k, f] = this.frameOf(d.door.open);
    d.img.setTexture(k, f);
    if (animate) { play('door'); this.effect('etincelle', d.door.x, d.door.y); this.cameras.main.shake(150, 0.003); }
  }

  /** Bulle de texte DOM au-dessus d'un sprite (texte net quel que soit le zoom). */
  bubble(sprite, text, ms = 2600, cls = '') {
    const el = document.createElement('div');
    el.className = 'bubble ' + cls;
    el.textContent = text.length > 120 ? text.slice(0, 117) + '…' : text;
    el.style.cssText = 'position:fixed;transform:translate(-50%,-100%);background:#f4e6c8;color:#2a1d16;border:3px solid #6b4430;border-radius:8px;padding:3px 9px;font:14px "JetBrains Mono",monospace;white-space:pre;max-width:420px;box-shadow:0 3px 0 rgba(0,0,0,.35)';
    this.bubbleLayer.appendChild(el);
    const b = { el, sprite, until: performance.now() + ms };
    this.bubbles.push(b);
    this.updateBubbles();
    return b;
  }

  updateBubbles() {
    const cam = this.cameras.main, now = performance.now();
    this.bubbles = this.bubbles.filter(b => {
      if (now > b.until || !b.sprite.active) { b.el.remove(); return false; }
      const x = (b.sprite.x - cam.worldView.x) * cam.zoom + cam.x, y = (b.sprite.y - 18 - cam.worldView.y) * cam.zoom + cam.y;
      b.el.style.left = x + 'px'; b.el.style.top = y + 'px';
      return true;
    });
  }

  clearBubbles() { this.bubbles.forEach(b => b.el.remove()); this.bubbles = []; }

  effect(type, tx, ty) {
    const x = tx * TS + 8, y = ty * TS + 8;
    const colors = { etincelle: [0xffd36e, 0xfff2b0], feu: [0xff7b39, 0xffd36e], eau: [0x5b8fe0, 0xb4dcff], fumee: [0x9a92ba, 0xd9d2ee],
      coeur: [0xff6b8a, 0xffc2d0], eclair: [0xfff7a8, 0xffffff], magie: [0xb88cff, 0xe8d8ff], poison: [0x7ccf6a, 0xc8f2b8] }[type] || [0xffffff, 0xffd36e];
    for (let i = 0; i < 14; i++) {
      const p = this.add.rectangle(x, y, 2, 2, colors[i % 2]).setDepth(9500);
      const a = Math.random() * Math.PI * 2, r = 6 + Math.random() * 14;
      this.tweens.add({ targets: p, x: x + Math.cos(a) * r, y: y + Math.sin(a) * r - 6, alpha: 0, duration: 500 + Math.random() * 400, onComplete: () => p.destroy() });
    }
    if (type === 'etincelle' || type === 'magie') play('sparkle');
  }

  // ---------- Acteurs pilotés par le code Java (scène de quête) ----------
  stageOrigin() { return this.stage?.origin || { x: Math.floor(this.hero.x / TS), y: Math.floor(this.hero.y / TS) }; }

  setupStage(stage) {
    this.clearStage();
    this.stage = stage || null;
    if (!stage) return;
    const o = this.stageOrigin();
    for (const [id, a] of Object.entries(stage.actors || {})) this.spawnActor(id, a.sprite, o.x + a.x, o.y + a.y, a);
    if (stage.focus) this.focusTile(o.x + stage.focus.x, o.y + stage.focus.y);
  }

  spawnActor(id, sprite, tx, ty, opts = {}) {
    const [k, f] = typeof sprite === 'number' ? ['dungeon', sprite] : this.frameOf(sprite.gid);
    const spr = this.add.image(tx * TS + 8, ty * TS + 15, k, f).setOrigin(0.5, 1).setDepth(ty * TS + 15);
    const a = { id, spr, x: tx, y: ty, solid: opts.solid !== false, home: { x: tx, y: ty }, sprite };
    this.actors.set(id, a);
    return a;
  }

  clearStage() {
    for (const a of this.actors.values()) a.spr.destroy();
    this.actors.clear();
    for (const t of this.tempTiles || []) t.destroy();
    this.tempTiles = [];
    this.stage = null;
    this.clearBubbles();
  }

  resetStage() { const s = this.stage; this.setupStage(s); }

  focusTile(tx, ty) {
    const cam = this.cameras.main;
    cam.stopFollow();
    cam.pan(tx * TS + 8, ty * TS + 8, 400, 'Sine.easeInOut');
  }

  followHero() { this.cameras.main.startFollow(this.hero, true, 0.15, 0.15); }

  /**
   * Joue un événement de la trace d'exécution. Retourne une promesse résolue à la fin de l'animation.
   * Vocabulaire : acteur.deplacer(id,dx,dy) · acteur.dire(id,texte) · acteur.apparaitre(id,sprite,x,y) · acteur.disparaitre(id)
   * · acteur.sprite(id,sprite) · tuile.poser(x,y,gid) · effet(type,x,y) · heros.dire(texte) · compteur(nom,valeur) · camera(x,y)
   */
  playEvent(ev, speed = 1) {
    const [name, args = []] = [ev.name, ev.args || []];
    const o = this.stageOrigin();
    const dur = (ms) => Math.max(40, ms / speed);
    return new Promise((resolve) => {
      const done = () => resolve();
      switch (name) {
        case 'acteur.deplacer': {
          const a = this.actors.get(args[0]);
          if (!a) return done();
          a.x += Number(args[1]) || 0; a.y += Number(args[2]) || 0;
          if (args[1]) a.spr.setFlipX(Number(args[1]) < 0);
          play('step', 0.9);
          this.tweens.add({ targets: a.spr, x: a.x * TS + 8, y: a.y * TS + 15, duration: dur(260), onUpdate: () => a.spr.setDepth(a.spr.y), onComplete: done });
          return;
        }
        case 'acteur.dire': {
          const a = this.actors.get(args[0]);
          if (a) this.bubble(a.spr, String(args[1] ?? ''), dur(2200));
          return this.time.delayedCall(dur(450), done);
        }
        case 'acteur.apparaitre': {
          const a = this.spawnActor(args[0], Number(args[1]), o.x + Number(args[2]), o.y + Number(args[3]));
          a.spr.setAlpha(0); this.tweens.add({ targets: a.spr, alpha: 1, duration: dur(250), onComplete: done });
          return;
        }
        case 'acteur.disparaitre': {
          const a = this.actors.get(args[0]);
          if (!a) return done();
          this.actors.delete(args[0]);
          this.effect('fumee', a.x, a.y);
          this.tweens.add({ targets: a.spr, alpha: 0, duration: dur(250), onComplete: () => { a.spr.destroy(); done(); } });
          return;
        }
        case 'acteur.sprite': {
          const a = this.actors.get(args[0]);
          if (a) a.spr.setFrame(Number(args[1]));
          return this.time.delayedCall(dur(150), done);
        }
        case 'tuile.poser': {
          const [k, f] = this.frameOf(Number(args[2]));
          const tx = o.x + Number(args[0]), ty = o.y + Number(args[1]);
          const img = this.add.image(tx * TS, ty * TS, k, f).setOrigin(0).setDepth(ty * TS + 1).setAlpha(0);
          this.tempTiles.push(img);
          play('drop');
          this.tweens.add({ targets: img, alpha: 1, duration: dur(200), onComplete: done });
          return;
        }
        case 'effet': this.effect(String(args[0]), o.x + Number(args[1] || 0), o.y + Number(args[2] || 0)); return this.time.delayedCall(dur(300), done);
        case 'heros.dire': this.bubble(this.hero, String(args[0] ?? ''), dur(2200)); return this.time.delayedCall(dur(450), done);
        case 'camera': this.focusTile(o.x + Number(args[0]), o.y + Number(args[1])); return this.time.delayedCall(dur(300), done);
        case 'compteur': this.hooks.onCounter?.(String(args[0]), args[1]); return this.time.delayedCall(dur(120), done);
        default: return this.time.delayedCall(dur(60), done);
      }
    });
  }
}

export { TS, TILE };
