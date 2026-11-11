// État de partie : création, migration de schéma, sauvegarde automatique (serveur local, écriture atomique).
import { api } from './api.js';

export const SCHEMA = 1;

export function newGame(name) {
  return {
    schema: SCHEMA,
    player: { name, createdAt: Date.now() },
    world: { map: 'village', x: null, y: null, facing: 'down', flags: {} },
    quests: {},      // id → { status, attempts, checks, hintsUsed, solutionShown, passedAt, drafts }
    evidence: [],    // preuves pédagogiques (voir pedagogy.js)
    reviews: {},     // calendrier de réactivation par compétence
    xp: 0,
    inventory: [],
    journal: [],     // événements narratifs marquants
    settings: { volume: 0.6, speed: 1 },
    stats: { playSeconds: 0, runs: 0, checks: 0 },
  };
}

/** Migration des anciennes versions de sauvegarde (aucune pour l'instant au-delà du schéma 1). */
export function migrate(save) {
  if (!save || typeof save !== 'object') throw new Error('Sauvegarde illisible');
  if (save.schema > SCHEMA) throw new Error('Sauvegarde créée par une version plus récente de JavaQuest');
  const base = newGame(save.player?.name || 'Aventurier');
  return { ...base, ...save, world: { ...base.world, ...(save.world || {}) }, settings: { ...base.settings, ...(save.settings || {}) },
    stats: { ...base.stats, ...(save.stats || {}) }, schema: SCHEMA };
}

export class Session {
  constructor(profileId, state) {
    this.profileId = profileId;
    this.state = state;
    this.timer = null;
    this.saving = false;
    this.dirty = false;
    this.listeners = new Set();
    this.lastError = null;
    this.tick = setInterval(() => { this.state.stats.playSeconds += 15; this.touch(); }, 15000);
  }

  quest(id) {
    return (this.state.quests[id] ||= { status: 'available', attempts: 0, checks: 0, hintsUsed: 0, solutionShown: false, drafts: null });
  }

  flag(name) { return !!this.state.world.flags[name]; }
  setFlag(name, value = true) { this.state.world.flags[name] = value; this.touch(); }

  onChange(fn) { this.listeners.add(fn); return () => this.listeners.delete(fn); }

  touch() {
    this.dirty = true;
    for (const fn of this.listeners) fn(this.state);
    if (!this.timer) this.timer = setTimeout(() => { this.timer = null; this.flush(); }, 1000);
  }

  /** Sauvegarde de dernière chance à la fermeture de l'onglet (requête keepalive). */
  flushOnExit() {
    if (!this.dirty) return;
    try { api.saveKeepalive(this.profileId, this.state); this.dirty = false; } catch { /* tant pis : la dernière sauvegarde régulière reste valable */ }
  }

  async flush() {
    if (!this.dirty) return;
    if (this.saving) { if (!this.timer) this.timer = setTimeout(() => { this.timer = null; this.flush(); }, 500); return; }
    this.saving = true;
    this.dirty = false;
    try {
      await api.save(this.profileId, this.state);
      this.lastError = null;
    } catch (e) {
      this.dirty = true;
      this.lastError = e;
      document.dispatchEvent(new CustomEvent('jq-save-error', { detail: e }));
    } finally {
      this.saving = false;
    }
  }
}
