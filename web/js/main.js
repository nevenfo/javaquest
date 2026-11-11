// JavaQuest — orchestration : écran titre, profils, monde, quêtes, récompenses, fenêtres.
import { api, initToken } from './api.js';
import { Session, newGame, migrate } from './state.js';
import { recordAttempt, skillStatus, LEVELS, suggestions, dueSkills, pickReview, questAvailable, DEFAULT_PARAMS } from './pedagogy.js';
import { WorldScene } from './world/scene.js';
import { MAPS, NPCS, AVATARS } from './world/maps.js';
import { Dialog } from './ui/dialog.js';
import { Workshop } from './ui/editor.js';
import { md, esc } from './ui/text.js';
import { play, setVolume } from './sound.js';
import { ExamHall } from './ui/exam.js';

const $ = (id) => document.getElementById(id);
const G = { content: null, quests: [], byId: new Map(), skills: new Map(), params: DEFAULT_PARAMS, session: null, scene: null, game: null };
window.JQ = G; // accès de débogage / tests E2E

// ---------- Utilitaires d'interface ----------
export function toast(text, cls = '') {
  const el = document.createElement('div');
  el.className = 'toast ' + cls; el.innerHTML = md(text, true);
  $('toasts').appendChild(el);
  setTimeout(() => el.remove(), 4200);
}

let sheet = null;
const portraitCache = new Map();
function portrait(sprite) {
  if (!sheet) return '';
  if (portraitCache.has(sprite)) return portraitCache.get(sprite);
  const c = document.createElement('canvas'); c.width = 64; c.height = 64;
  const g = c.getContext('2d'); g.imageSmoothingEnabled = false;
  g.drawImage(sheet, (sprite % 12) * 16, Math.floor(sprite / 12) * 16, 16, 16, 0, 0, 64, 64);
  const url = c.toDataURL(); portraitCache.set(sprite, url); return url;
}

function modal(html, { onClose, wide } = {}) {
  const m = $('modal'), box = $('modal-box');
  box.innerHTML = `<div class="modal">${html}</div>`;
  box.style.width = wide ? 'min(1200px,96vw)' : '';
  m.hidden = false;
  G.scene?.freeze(true);
  play('open');
  const close = () => { m.hidden = true; G.scene?.freeze(dialogOpen()); onClose?.(); play('close'); removeEventListener('keydown', esc1); };
  const esc1 = (e) => { if (e.code === 'Escape') { e.preventDefault(); close(); } };
  setTimeout(() => addEventListener('keydown', esc1), 0);
  box.querySelectorAll('[data-close]').forEach(b => b.onclick = close);
  return { box, close };
}

const dialog = new Dialog();
const dialogOpen = () => dialog.open || G.workshop?.open || !$('modal').hidden;

// ---------- Démarrage ----------
async function boot() {
  if (!initToken()) return fatal('Session introuvable. Lance le jeu avec <b>JavaQuest.exe</b> (le lien contient un jeton de sécurité).');
  try {
    const h = await api.health();
    if (!h.sandbox) toast(`⚠ ${h.sandboxError}`, 'bad');
    G.content = await api.content();
  } catch (e) {
    return fatal(`Impossible de joindre le serveur local (${esc(e.message)}). Relance <b>JavaQuest.exe</b>.`);
  }
  G.quests = G.content.quests;
  G.quests.forEach((q, i) => { q.order = i; G.byId.set(q.id, q); });
  for (const s of G.content.skills?.skills || []) G.skills.set(s.id, s);
  G.params = { ...DEFAULT_PARAMS, ...(G.content.skills?.params || {}) };
  sheet = new Image(); sheet.src = 'assets/tiles/dungeon.png';
  await new Promise(r => { sheet.onload = r; sheet.onerror = r; });
  titleScreen();
}

function fatal(html) { const f = $('fatal'); f.innerHTML = `<div>${html}</div>`; f.hidden = false; }

async function titleScreen() {
  let profiles = [];
  try { profiles = (await api.profiles()).profiles; } catch (e) { toast(e.message, 'bad'); }
  const list = profiles.map(p => `<div class="card profile"><div><b>${esc(p.name)}</b>${p.corrupted ? ' <span class="tag lvl-consolider">illisible</span>' : ''}<div class="muted">${p.updated ? 'Dernière partie : ' + new Date(p.updated).toLocaleString('fr-FR') : ''}</div></div>
     <div class="row"><button class="btn primary" data-play="${esc(p.id)}">Continuer</button><button class="btn ghost" data-del="${esc(p.id)}" title="Supprimer (récupérable dans le dossier de sauvegarde)">🗑</button></div></div>`).join('');
  const avatars = AVATARS.map((a, i) => `<label style="text-align:center;cursor:pointer"><input type="radio" name="av" value="${a.sprite}" ${i === 0 ? 'checked' : ''} style="display:block;margin:auto"><img src="${portrait(a.sprite)}" width="48" height="48" style="image-rendering:pixelated"><div class="muted">${a.label}</div></label>`).join('');
  const { box, close } = modal(`<div class="title-screen"><div class="logo">JavaQuest</div><div class="sub">Les Runes de Javelune — apprends Java en écrivant de vrais sorts</div></div>
    ${profiles.length ? `<h2>Reprendre une aventure</h2>${list}` : ''}
    <h2>Nouvelle aventure</h2>
    <div class="card"><div class="row"><input type="text" id="np-name" maxlength="30" placeholder="Ton nom d'aventurier·ère" autofocus> <button class="btn primary" id="np-go">Commencer</button></div>
    <div class="row" style="margin-top:10px;gap:18px">${avatars}</div></div>
    <p class="muted">Tout reste sur ce PC : aucune connexion, aucun compte. Chaque profil a sa propre sauvegarde.</p>`);
  G.scene?.freeze(true);
  box.querySelectorAll('[data-play]').forEach(b => b.onclick = async () => { close(); await startProfile(b.dataset.play); });
  box.querySelectorAll('[data-del]').forEach(b => b.onclick = async () => {
    if (!confirm('Supprimer ce profil ? (Le fichier est conservé sous un autre nom dans le dossier de sauvegarde.)')) return;
    await api.deleteProfile(b.dataset.del); close(); titleScreen();
  });
  const go = async () => {
    const name = $('np-name').value.trim();
    if (!name) return $('np-name').focus();
    const sprite = +box.querySelector('input[name=av]:checked').value;
    const { id } = await api.createProfile(name);
    const state = newGame(name); state.player.avatarSprite = sprite;
    await api.save(id, state);
    close(); await startProfile(id, true);
  };
  $('np-go').onclick = go;
  $('np-name').onkeydown = (e) => { if (e.key === 'Enter') go(); };
}

async function startProfile(id, fresh = false) {
  let state;
  try { state = migrate(await api.load(id)); } catch (e) { return toast('Sauvegarde illisible : ' + e.message, 'bad'); }
  G.session?.flush();
  G.session = new Session(id, state);
  setVolume(state.settings.volume);
  document.addEventListener('jq-save-error', (e) => toast(`⚠ Sauvegarde impossible : ${esc(e.detail.message)} — ta progression reste en mémoire, réessaie (Échap › Sauvegarder).`, 'bad'));
  if (!G.game) await startGame(); else G.scene.loadMap(state.world.map || 'village');
  $('hud').hidden = false;
  updateHud();
  G.session.onChange(() => updateHud());
  if (fresh) intro();
}

function startGame() {
  return new Promise((resolve) => {
    const scene = new WorldScene({
      session: { get state() { return G.session.state; }, touch: () => G.session.touch() },
      ready: (s) => { G.scene = s; s.loadMap(G.session.state.world.map || 'village'); resolve(); },
      onInteract: interact,
      onEnterMap: (id, def) => {
        const w = G.session.state.world; w.visited ||= []; if (!w.visited.includes(id)) w.visited.push(id);
        updateHud(); toast(`📍 ${def.name}`, 'gold');
      },
      onBlocked: (msg) => { play('error'); toast(msg, 'bad'); },
      isDone: (req) => isDone(req),
      npcMarker: (npcId) => npcMarker(npcId),
      doorLabel: (id) => doorLabel(id),
      extraObjects: (mapId) => questObjects(mapId),
      panelWidth: () => G.workshop?.open ? $('editor').offsetWidth : 0,
      onCounter: (name, value) => toast(`${name} : ${value}`, 'gold'),
    });
    G.game = new Phaser.Game({ type: Phaser.AUTO, parent: 'game', pixelArt: true, backgroundColor: '#1d1a2b',
      scale: { mode: Phaser.Scale.RESIZE, width: innerWidth, height: innerHeight }, scene: [scene], audio: { noAudio: true } });
    G.workshop = new Workshop({
      session: { get state() { return G.session.state; }, quest: (id) => G.session.quest(id), touch: () => G.session.touch() },
      scene: () => G.scene, onPass, onFail, toast,
      onClose: () => { G.scene.freeze(false); updateHud(); G.scene.refreshMarkers(); },
      layout: () => { G.scene?.layout(); },
      lessonFor, skillTitle: (id) => G.skills.get(id)?.title || id,
    });
    G.exam = new ExamHall({ G, modal, toast, api, play });
  });
}

function isDone(req) {
  const s = G.session.state;
  return s.quests[req]?.status === 'done' || !!s.world.flags[req];
}

function questObjects(mapId) {
  return G.quests.filter(q => q.object && q.object.map === mapId).map(q => ({ ...q.object, quest: q.id, label: q.object.label || `Examiner : ${q.title}` }));
}

function npcQuests(npcId) { return G.quests.filter(q => q.giver === npcId); }

function npcMarker(npcId) {
  const s = G.session?.state;
  if (!s) return '';
  const qs = npcQuests(npcId).filter(q => s.quests[q.id]?.status !== 'done');
  if (!qs.length) return '';
  const q = qs[0];
  if (!questAvailable(s, q)) return '';
  return s.quests[q.id]?.status === 'active' ? '…' : '!';
}

function doorLabel(id) {
  const q = G.byId.get(MAPS[G.scene.mapId].doorQuests?.[id]);
  if (G.session.state.world.flags['door:' + id]) return id === 'sanctuaire' ? 'Entrer dans la Citadelle des Épreuves' : 'Porte ouverte';
  return q ? `Examiner la porte (${q.title})` : 'La porte est fermée';
}

// ---------- Interactions ----------
async function interact(t) {
  if (dialogOpen()) return;
  G.scene.freeze(true);
  try {
    if (t.kind === 'npc') await talk(t.npc.id);
    else if (t.kind === 'object') await useObject(t.obj);
    else if (t.kind === 'door') await useDoor(t.id);
  } finally { if (!G.workshop.open && $('modal').hidden) G.scene.freeze(false); G.scene.refreshMarkers(); }
}

function speaker(npcId) { const n = NPCS[npcId]; return { name: n.name, portrait: portrait(n.sprite) }; }

async function offerQuest(q, who) {
  const s = G.session.state, st = s.quests[q.id]?.status;
  const pages = (st === 'active' ? [q.dialog?.resume || 'Alors, ce sort avance ?'] : (q.dialog?.intro || [q.brief])).map(t => ({ text: t }));
  const last = pages[pages.length - 1];
  last.choices = [{ label: st === 'active' ? '✍ Reprendre l\'écriture du sort' : `✍ Accepter : « ${q.title} »`, value: 'go' }, { label: 'Plus tard', value: 'later' }];
  const r = await dialog.show(who, pages);
  if (r === 'go') openQuest(q);
}

function openQuest(q) {
  G.scene.freeze(true);
  G.session.quest(q.id);
  updateHud(q);
  G.workshop.openQuest(q);
}

async function talk(npcId) {
  const s = G.session.state, who = speaker(npcId);
  const qs = npcQuests(npcId);
  const next = qs.find(q => s.quests[q.id]?.status !== 'done' && questAvailable(s, q));
  if (next) return offerQuest(next, who);
  const locked = qs.find(q => s.quests[q.id]?.status !== 'done');
  if (locked) {
    const need = (locked.requires || []).filter(r => s.quests[r]?.status !== 'done').map(r => G.byId.get(r)?.title || r);
    return dialog.show(who, [locked.dialog?.locked || `J'aurai une mission pour toi… mais termine d'abord : ${need.map(n => `« ${n} »`).join(', ')}.`]);
  }
  const lastDone = [...qs].reverse().find(q => s.quests[q.id]?.status === 'done');
  const lines = NPCS[npcId].barks || [lastDone?.dialog?.done || `Je suis ${NPCS[npcId].name}, ${NPCS[npcId].role.toLowerCase()}. Les runes de Java t'attendent ailleurs, aventurier·ère !`];
  return dialog.show(who, [lines[Math.floor(Math.random() * lines.length)]]);
}

async function useObject(o) {
  if (o.action === 'board') return reviewBoard();
  if (o.action === 'grimoire') return grimoire();
  if (o.action === 'exam') return G.exam.menu();
  if (o.quest) {
    const q = G.byId.get(o.quest), s = G.session.state;
    if (!questAvailable(s, q)) return dialog.show({ name: o.label }, [q.dialog?.locked || 'Rien ne se passe… pas encore.']);
    if (s.quests[q.id]?.status === 'done' && q.assessment !== 'review') return dialog.show({ name: q.title }, [q.dialog?.done || 'Le sort est accompli.']);
    return offerQuest(q, { name: q.object?.name || q.title, portrait: q.object?.portrait ? portrait(q.object.portrait) : '' });
  }
}

async function useDoor(id) {
  const s = G.session.state;
  if (s.world.flags['door:' + id]) {
    if (id === 'sanctuaire') return G.exam.enter();
    return;
  }
  const qid = MAPS[G.scene.mapId].doorQuests?.[id];
  if (qid) return offerQuest(G.byId.get(qid), { name: 'Porte scellée', portrait: '' });
  play('latch');
  toast('La porte est verrouillée.', '');
}

// ---------- Résultats de quête ----------
async function onPass(quest, { assisted }) {
  const s = G.session.state, qs = G.session.quest(quest.id);
  const before = Object.fromEntries((quest.skills || []).map(k => [k, skillStatus(s, k, Date.now(), G.params).level]));
  recordAttempt(s, quest, { ok: true, assisted, params: G.params });
  const first = qs.status !== 'done';
  qs.status = 'done';
  qs.passes = (qs.passes || 0) + 1;
  qs.passedAt ||= Date.now();
  qs.lastAssisted = assisted;
  let rewardText = '';
  if (first) {
    const xp = quest.reward?.xp ?? 10;
    s.xp += xp; rewardText = `+${xp} XP`;
    for (const eff of quest.effects?.onPass || []) applyEffect(eff);
    s.journal.push({ at: Date.now(), text: `Quête accomplie : ${quest.title}` });
  }
  // Remise à zéro de l'aide pour une prochaine tentative autonome éventuelle.
  qs.hintsUsed = 0; qs.solutionShown = false;
  G.session.touch();
  updateHud(); G.scene.refreshMarkers();
  const lines = (quest.skills || []).map(k => {
    const st = skillStatus(s, k, Date.now(), G.params);
    const changed = st.level !== before[k];
    return `- **${esc(G.skills.get(k)?.title || k)}** : ${LEVELS[st.level].label}${changed ? ' ⬆' : ''}${st.missing.length && st.level !== 'maitrise' ? ` — pour la maîtrise : ${st.missing.join(', ')}` : ''}`;
  }).join('\n');
  const who = quest.giver ? speaker(quest.giver) : { name: quest.title, portrait: '' };
  const success = quest.dialog?.success || ['Magnifique ! Le sort fonctionne.'];
  const pages = [...success.map(t => ({ text: t }))];
  pages.push({ text: `${rewardText ? `**Récompense** : ${rewardText}. ` : ''}${assisted ? 'Réussite **avec aide** : bravo ! Pour prouver la compétence seul·e, une autre épreuve t\'attendra (tableau des primes).' : 'Réussite **autonome** : c\'est une vraie preuve de compétence.'}\n\n${lines}` });
  G.workshop.close();
  G.scene.freeze(true);
  await dialog.show(who, pages);
  G.scene.freeze(false);
}

function onFail(quest, { assisted }) {
  const s = G.session.state;
  if (quest.assessment === 'review' || quest.assessment === 'exam') recordAttempt(s, quest, { ok: false, assisted, params: G.params });
  const qs = G.session.quest(quest.id);
  qs.attempts = (qs.attempts || 0) + 1;
  G.session.touch();
  if (qs.attempts === 3 && (quest.hints || []).length && !qs.hintsUsed) toast('Besoin d\'un coup de pouce ? L\'onglet **Indices** propose une aide progressive.', 'gold');
}

function applyEffect(eff) {
  const [kind, a, b] = eff.split(':');
  const s = G.session.state;
  if (kind === 'flag') s.world.flags[a] = true;
  else if (kind === 'door') { s.world.flags['door:' + a] = true; G.scene.openDoor(a); }
  else if (kind === 'item') { s.inventory.push({ id: a, name: b || a, at: Date.now() }); toast(`🎒 Objet obtenu : ${b || a}`, 'gold'); }
  else if (kind === 'unlock') toast(`🔓 ${a}`, 'gold');
}

// ---------- HUD ----------
function trackedQuest() {
  const s = G.session.state;
  const active = G.quests.filter(q => s.quests[q.id]?.status === 'active');
  if (active.length) return active[active.length - 1];
  return G.quests.find(q => q.main && s.quests[q.id]?.status !== 'done' && questAvailable(s, q));
}

function updateHud(focus) {
  if (!G.session) return;
  const s = G.session.state;
  $('hud-place').textContent = MAPS[s.world.map]?.name || '';
  $('hud-xp').textContent = `✨ ${s.xp} XP`;
  const q = focus || trackedQuest();
  const due = dueSkills(s).length;
  $('hud-quest').innerHTML = (q ? `📌 <b>${esc(q.title)}</b>${q.giver ? ` — ${esc(NPCS[q.giver]?.name || '')}` : ''}${q.hud ? `<br><small>${esc(q.hud)}</small>` : ''}` : '📌 Explore Javelune') + (due ? `<br><small>📋 ${due} révision(s) au tableau des primes</small>` : '');
}

// ---------- Fenêtres ----------
function lessonFor(q) {
  let h = q.lesson ? md(q.lesson) : '';
  for (const k of q.skills || []) { const sk = G.skills.get(k); if (sk?.lesson) h += `<h4 style="margin:8px 0 4px">${esc(sk.title)}</h4>` + md(sk.lesson); }
  return h;
}

function journal() {
  const s = G.session.state;
  const row = (q) => {
    const st = s.quests[q.id]?.status || (questAvailable(s, q) ? 'available' : 'locked');
    const label = { done: '✔ accomplie', active: '✍ en cours', available: '❕ disponible', locked: '🔒 verrouillée' }[st];
    const where = q.giver ? NPCS[q.giver]?.name : q.object ? 'objet du monde' : '';
    return `<div class="card"><b>${esc(q.title)}</b> <span class="tag">${label}</span> <span class="muted">${esc(MAPS[q.region]?.name || q.region || '')}${where ? ' · ' + esc(where) : ''}</span>
      ${st === 'active' || (st === 'available' && q.object) ? `<button class="btn" data-q="${esc(q.id)}" style="float:right">Ouvrir</button>` : ''}<div class="muted">${md(q.summary || '', true)}</div></div>`;
  };
  const main = G.quests.filter(q => q.main), side = G.quests.filter(q => !q.main && q.assessment !== 'review' && q.assessment !== 'exam');
  const { box, close } = modal(`<h1>📜 Journal</h1><h2>Quête principale</h2>${main.map(row).join('')}<h2>Quêtes secondaires</h2>${side.map(row).join('') || '<p class="muted">Aucune pour l\'instant.</p>'}
    <h2>🎒 Sac</h2>${s.inventory.length ? '<div class="row">' + s.inventory.map(it => `<span class="tag" title="Obtenu le ${new Date(it.at).toLocaleDateString('fr-FR')}">${esc(it.name)}</span>`).join('') + '</div>' : '<p class="muted">Ton sac est vide.</p>'}
    <h2>Chronique</h2>${s.journal.slice(-12).reverse().map(j => `<div class="muted">${new Date(j.at).toLocaleString('fr-FR')} — ${esc(j.text)}</div>`).join('') || '<p class="muted">Ton histoire commence.</p>'}
    <p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
  box.querySelectorAll('[data-q]').forEach(b => b.onclick = () => { close(); openQuest(G.byId.get(b.dataset.q)); });
}

function grimoire() {
  const s = G.session.state, now = Date.now();
  const byRegion = new Map();
  for (const sk of G.skills.values()) { const r = sk.region || 'autre'; if (!byRegion.has(r)) byRegion.set(r, []); byRegion.get(r).push(sk); }
  let h = `<h1>📘 Grimoire des compétences</h1><p class="muted">Deux progressions distinctes : l'<b>XP</b> récompense ton aventure ; la <b>maîtrise</b> exige des preuves autonomes, un transfert et une réussite différée (règles détaillées dans le menu Aide). Une réussite avec indice est encouragée mais ne prouve pas encore la maîtrise.</p>`;
  const counts = { maitrise: 0, encours: 0, decouverte: 0, consolider: 0, nouveau: 0 };
  for (const [region, list] of byRegion) {
    h += `<h2>${esc(MAPS[region]?.name || region)}</h2><div class="skill-grid">`;
    for (const sk of list) {
      const st = skillStatus(s, sk.id, now, G.params); counts[st.level]++;
      h += `<div class="card"><b>${esc(sk.title)}</b><br><span class="tag ${LEVELS[st.level].css}">${LEVELS[st.level].label}</span>${st.isDue ? '<span class="tag lvl-consolider">révision due</span>' : ''}
        <div class="muted">${esc(sk.objective || '')}</div>${st.level !== 'maitrise' && st.level !== 'nouveau' && st.missing.length ? `<div class="muted">Il manque : ${esc(st.missing.join(' ; '))}</div>` : ''}
        ${sk.lesson ? `<details><summary>Fiche</summary>${md(sk.lesson)}</details>` : ''}${sk.source ? `<div class="muted">Programme : ${esc(sk.source)}</div>` : ''}</div>`;
    }
    h += '</div>';
  }
  h = h.replace('</p>', `</p><p><span class="tag lvl-maitrise">${counts.maitrise} maîtrisée(s)</span><span class="tag lvl-encours">${counts.encours} en cours</span><span class="tag lvl-decouverte">${counts.decouverte} découverte(s)</span><span class="tag lvl-consolider">${counts.consolider} à consolider</span><span class="tag lvl-nouveau">${counts.nouveau} à découvrir</span></p>`);
  modal(h + '<p><button class="btn" data-close>Fermer</button></p>', { wide: true });
}

function reviewBoard() {
  const s = G.session.state, now = Date.now();
  const due = dueSkills(s, now);
  const dueItems = due.map(k => ({ k, q: pickReview(s, G.quests, k) })).filter(x => x.q);
  // Entraînement mélangé (entrelacement) : compétences déjà pratiquées en autonomie, de thèmes différents.
  const practiced = Object.keys(s.reviews || {}).filter(k => !due.includes(k));
  const mixed = practiced.map(k => ({ k, q: pickReview(s, G.quests, k) })).filter(x => x.q).sort(() => Math.random() - 0.5).slice(0, 3);
  const sug = suggestions(s, G.quests, now).filter(x => x.type !== 'review').slice(0, 4);
  const item = (x, label) => `<div class="card"><b>${esc(x.q.title)}</b> <span class="tag">${label}</span> <span class="muted">${esc(G.skills.get(x.k)?.title || '')}</span><button class="btn primary" data-q="${esc(x.q.id)}" style="float:right">Relever le défi</button><div class="muted">${md(x.q.summary || '', true)}</div></div>`;
  const { box, close } = modal(`<h1>📋 Tableau des primes</h1>
    <p class="muted">Réactiver une compétence quelques jours plus tard, sans aide, consolide la mémoire (pratique espacée et récupération). Les intervalles (1, 3, 7, 21 jours) sont des réglages de départ, pas des valeurs scientifiquement optimales.</p>
    <h2>Révisions dues (${dueItems.length})</h2>${dueItems.map(x => item(x, 'à réactiver')).join('') || '<p class="muted">Rien d\'urgent. Reviens plus tard !</p>'}
    <h2>Entraînement mélangé</h2>${mixed.map(x => item(x, 'entraînement')).join('') || '<p class="muted">Réussis des quêtes en autonomie pour débloquer l\'entraînement.</p>'}
    <h2>Prochaines aventures conseillées</h2>${sug.map(x => `<div class="card"><b>${esc(x.quest.title)}</b> <span class="muted">${esc(x.quest.giver ? 'auprès de ' + NPCS[x.quest.giver]?.name : '')} — ${esc(MAPS[x.quest.region]?.name || '')}</span></div>`).join('') || '<p class="muted">—</p>'}
    <p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
  box.querySelectorAll('[data-q]').forEach(b => b.onclick = () => { close(); const q = G.byId.get(b.dataset.q); G.session.quest(q.id).drafts = null; openQuest(q); });
}

function worldMap() {
  const s = G.session.state, visited = s.world.visited || [];
  const nodes = Object.entries(MAPS).map(([id, m]) => {
    const seen = visited.includes(id);
    return `<div class="mapnode ${seen ? '' : 'locked'} ${s.world.map === id ? 'here' : ''}" style="left:${m.mapPos.x}%;top:${m.mapPos.y}%">${seen ? esc(m.name) : '???'}
      <small>${s.world.map === id ? 'Tu es ici' : seen ? '' : 'inexploré'}</small>${seen && s.world.map !== id ? `<button class="btn" data-go="${id}" style="margin-top:4px;padding:2px 8px">Voyager</button>` : ''}</div>`;
  }).join('');
  const { box, close } = modal(`<h1>🗺 Carte de Javelune</h1><div class="mapgrid">${nodes}</div><p class="muted">Voyage rapide vers les lieux déjà visités.</p><p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
  box.querySelectorAll('[data-go]').forEach(b => b.onclick = () => { close(); G.scene.loadMap(b.dataset.go); });
}

function menu() {
  const s = G.session.state;
  const { box, close } = modal(`<h1>☰ Menu</h1>
    <div class="row"><button class="btn primary" data-close>Reprendre</button><button class="btn" id="m-save">💾 Sauvegarder</button><button class="btn" id="m-export">⬇ Exporter ma sauvegarde</button>
    <label class="btn">⬆ Importer<input type="file" id="m-import" accept=".json" hidden></label><button class="btn" id="m-profiles">👥 Changer de profil</button></div>
    <h2>Options</h2><div class="row">Volume <input type="range" id="m-vol" min="0" max="1" step="0.1" value="${s.settings.volume}"> Vitesse des animations du code <input type="range" id="m-speed" min="0.5" max="3" step="0.5" value="${s.settings.speed}"></div>
    <h2>Commandes</h2><p>Déplacement : <kbd>Z Q S D</kbd> (AZERTY) / <kbd>W A S D</kbd> (QWERTY) / flèches · Interagir : <kbd>E</kbd> · Journal <kbd>J</kbd> · Grimoire <kbd>G</kbd> · Carte <kbd>M</kbd> · Éditeur : <kbd>Ctrl+Entrée</kbd> exécuter, <kbd>Ctrl+Maj+Entrée</kbd> vérifier, <kbd>Ctrl+S</kbd> sauvegarder.</p>
    <h2>Comment est mesurée la maîtrise ?</h2><p>Une compétence est « maîtrisée » quand tu l'as démontrée <b>seul·e</b> (sans indice) sur au moins ${G.params.masteryMinQuests} activités différentes, dont un <b>transfert</b> (problème nouveau), et à nouveau après au moins ${G.params.masteryMinDelayDays} jour(s). Un échec isolé ne retire rien. Ces règles sont des hypothèses de conception prudentes inspirées de la recherche (pratique de récupération, espacement, apprentissage par maîtrise), pas des seuils démontrés.</p>
    <h2>Sécurité</h2><p class="muted">Ton code Java est compilé par un vrai javac (JDK 25) puis exécuté dans un conteneur isolé de Windows (AppContainer sans réseau ni accès à tes fichiers, mémoire/temps limités).</p>
    <h2>Crédits</h2><p class="muted">Graphismes et sons : Kenney (CC0). Moteur : Phaser 3 (MIT). Éditeur : CodeMirror 5 (MIT). Polices : JetBrains Mono, Silkscreen (OFL). Runtime : Eclipse Temurin 25 (GPLv2 + CE). Voir THIRD_PARTY.md.</p>`);
  $('m-save').onclick = async () => { G.session.dirty = true; await G.session.flush(); toast(G.session.lastError ? 'Échec de la sauvegarde.' : 'Partie sauvegardée.', G.session.lastError ? 'bad' : 'good'); };
  $('m-export').onclick = () => { const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([JSON.stringify(s, null, 1)], { type: 'application/json' })); a.download = `javaquest-${G.session.profileId}.json`; a.click(); };
  $('m-import').onchange = async (e) => {
    try { const data = migrate(JSON.parse(await e.target.files[0].text())); if (!confirm('Remplacer la progression de ce profil par le fichier importé ?')) return; G.session.state = data; G.session.touch(); await G.session.flush(); location.reload(); }
    catch (err) { toast('Import impossible : ' + err.message, 'bad'); }
  };
  $('m-profiles').onclick = async () => { await G.session.flush(); close(); titleScreen(); };
  $('m-vol').oninput = (e) => { s.settings.volume = +e.target.value; setVolume(s.settings.volume); G.session.touch(); play('tick'); };
  $('m-speed').oninput = (e) => { s.settings.speed = +e.target.value; G.session.touch(); };
}

async function intro() {
  G.scene.freeze(true);
  await dialog.show(speaker('ysolde'), [
    'Ah, te voilà enfin, jeune apprenti·e ! Je suis Ysolde, gardienne du Sanctuaire de la Compilation.',
    'À Javelune, la magie s\'écrit en **Java** : chaque sort est un programme. La Grande Machine Virtuelle le comprend… à condition qu\'il soit écrit avec rigueur.',
    'Approche-toi de moi (touches **Z Q S D** ou flèches) et appuie sur **E** pour me parler. Ta première épreuve t\'attend devant le Sanctuaire, juste au nord.',
  ]);
  G.scene.freeze(false);
}

// ---------- Raccourcis globaux ----------
addEventListener('keydown', (e) => {
  if (!G.session || !G.scene) return;
  const typing = e.target.closest?.('input,textarea,.CodeMirror');
  if (typing) return;
  if (dialog.open) return;
  if (!$('modal').hidden || G.workshop.open) return;
  if (e.code === 'KeyE' || e.code === 'Space' || e.code === 'Enter') { e.preventDefault(); G.scene.interact(); }
  else if (e.code === 'KeyJ') journal();
  else if (e.code === 'KeyG') grimoire();
  else if (e.code === 'KeyM') worldMap();
  else if (e.code === 'Escape') menu();
});
document.querySelectorAll('#hud [data-open]').forEach(b => b.onclick = () => ({ journal, grimoire, map: worldMap, menu })[b.dataset.open]());
addEventListener('pagehide', () => G.session?.flushOnExit());
document.addEventListener('visibilitychange', () => { if (document.hidden) G.session?.flush(); });

boot();
