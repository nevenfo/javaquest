// Moteur pédagogique : preuves, niveaux de maîtrise, réactivations espacées, suggestions.
// Fonctions pures (testées par tests/web/pedagogy.test.mjs). Les règles chiffrées sont des
// HYPOTHÈSES DE CONCEPTION paramétrables (docs/PEDAGOGIE.md §2), pas des valeurs démontrées.

export const DEFAULT_PARAMS = {
  reviewIntervalsDays: [1, 3, 7, 21], // H5
  masteryMinQuests: 2,                // H1 : preuves autonomes sur ≥ 2 activités distinctes
  masteryRequiresTransfer: true,      // H1 : dont ≥ 1 transfert (ou examen)
  masteryMinDelayDays: 1,             // H1 : + une réussite différée ≥ 1 jour après la première preuve autonome
};

const DAY = 86400000;
const AUTONOMOUS = new Set(['autonomous', 'transfer', 'review', 'exam']);

export const LEVELS = {
  nouveau: { label: 'Pas encore rencontrée', css: 'lvl-nouveau' },
  decouverte: { label: 'Découverte (guidée)', css: 'lvl-decouverte' },
  encours: { label: 'En cours (autonome)', css: 'lvl-encours' },
  maitrise: { label: 'Maîtrise démontrée', css: 'lvl-maitrise' },
  consolider: { label: 'À consolider', css: 'lvl-consolider' },
};

/** Une tentative est « autonome » si le format l'est et qu'aucune aide n'a été utilisée. */
export function isAutonomous(ev) {
  return ev.ok && !ev.assisted && AUTONOMOUS.has(ev.assessment);
}

/**
 * Enregistre le résultat d'une vérification de quête dans l'état (mutation) :
 * une preuve par compétence + mise à jour du calendrier de réactivation.
 */
export function recordAttempt(state, quest, { ok, assisted, at = Date.now(), params = DEFAULT_PARAMS }) {
  state.evidence ||= [];
  state.reviews ||= {};
  const assessment = quest.assessment || 'practice';
  for (const skill of quest.skills || []) {
    const ev = { skill, quest: quest.id, at, ok: !!ok, assisted: !!assisted, assessment };
    state.evidence.push(ev);
    const r = state.reviews[skill];
    const iv = params.reviewIntervalsDays;
    if (assessment === 'review' && r) {
      if (ok && !assisted) { r.step = Math.min(r.step + 1, iv.length - 1); r.due = at + iv[r.step] * DAY; }
      else if (!ok) { r.step = Math.max(0, r.step - 1); r.due = at + iv[0] * DAY; r.lapses = (r.lapses || 0) + 1; }
      r.last = at;
    } else if (isAutonomous(ev) && !r) {
      state.reviews[skill] = { step: 0, due: at + iv[0] * DAY, last: at, lapses: 0 };
    }
  }
  // Limite de taille : on garde l'essentiel (les 4000 dernières preuves).
  if (state.evidence.length > 4000) state.evidence.splice(0, state.evidence.length - 4000);
}

/** Niveau d'une compétence, avec l'explication lisible de ce qui manque. */
export function skillStatus(state, skill, now = Date.now(), params = DEFAULT_PARAMS) {
  const evs = (state.evidence || []).filter(e => e.skill === skill);
  const auto = evs.filter(isAutonomous);
  const guidedOk = evs.some(e => e.ok);
  const review = (state.reviews || {})[skill] || null;
  const quests = new Set(auto.map(e => e.quest));
  const transfer = auto.some(e => e.assessment === 'transfer' || e.assessment === 'exam');
  const first = auto.length ? Math.min(...auto.map(e => e.at)) : null;
  const delayed = first !== null && auto.some(e => e.at - first >= params.masteryMinDelayDays * DAY);
  const missing = [];
  if (quests.size < params.masteryMinQuests) missing.push(`réussir seul·e ${params.masteryMinQuests - quests.size} activité(s) de plus`);
  if (params.masteryRequiresTransfer && !transfer) missing.push('réussir un problème de transfert (nouveau contexte)');
  if (!delayed) missing.push(`réussir à nouveau après au moins ${params.masteryMinDelayDays} jour(s)`);
  let level;
  if (!evs.length) level = 'nouveau';
  else if (!auto.length) level = guidedOk ? 'decouverte' : 'nouveau';
  else if (missing.length) level = 'encours';
  else level = 'maitrise';
  // H2 : un échec en réactivation ne retire pas la maîtrise, il la marque « à consolider ».
  const lastReview = evs.filter(e => e.assessment === 'review').sort((a, b) => b.at - a.at)[0];
  if (level === 'maitrise' && lastReview && !lastReview.ok) level = 'consolider';
  return {
    level, missing,
    autonomousQuests: quests.size, transfer, delayed,
    attempts: evs.length, failures: evs.filter(e => !e.ok).length,
    due: review ? review.due : null, isDue: !!review && review.due <= now,
  };
}

/** Compétences dont la réactivation est due, les plus en retard d'abord. */
export function dueSkills(state, now = Date.now()) {
  return Object.entries(state.reviews || {})
    .filter(([, r]) => r.due <= now)
    .sort((a, b) => a[1].due - b[1].due)
    .map(([skill]) => skill);
}

/**
 * Choisit une activité de réactivation pour une compétence : de préférence une quête « review »
 * jamais faite ou faite il y a le plus longtemps ; sinon la quête autonome la plus ancienne.
 */
export function pickReview(state, quests, skill) {
  const last = (id) => Math.max(0, ...(state.evidence || []).filter(e => e.quest === id).map(e => e.at));
  const pool = quests.filter(q => (q.skills || []).includes(skill) && q.assessment === 'review');
  const fallback = quests.filter(q => (q.skills || []).includes(skill) && AUTONOMOUS.has(q.assessment) && q.assessment !== 'exam');
  const list = (pool.length ? pool : fallback).slice().sort((a, b) => last(a.id) - last(b.id));
  return list[0] || null;
}

/** Une quête est disponible si ses quêtes prérequises sont terminées. */
export function questAvailable(state, quest) {
  return (quest.requires || []).every(id => state.quests?.[id]?.status === 'done');
}

/** Prochaines activités conseillées : réactivations dues, puis quêtes principales disponibles. */
export function suggestions(state, quests, now = Date.now()) {
  const out = [];
  for (const skill of dueSkills(state, now).slice(0, 3)) {
    const q = pickReview(state, quests, skill);
    if (q) out.push({ type: 'review', skill, quest: q });
  }
  for (const q of quests) {
    if (q.assessment === 'review' || q.assessment === 'exam') continue;
    if (state.quests?.[q.id]?.status === 'done' || !questAvailable(state, q)) continue;
    out.push({ type: q.main ? 'main' : 'side', quest: q });
    if (out.length >= 6) break;
  }
  return out;
}
