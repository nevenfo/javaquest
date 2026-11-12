// Tests du moteur pédagogique (node --test tests/web). Vérifie les hypothèses H1–H5 de docs/PEDAGOGIE.md.
import test from 'node:test';
import assert from 'node:assert/strict';
import { recordAttempt, skillStatus, dueSkills, pickReview, questAvailable, suggestions, DEFAULT_PARAMS } from '../../web/js/pedagogy.js';

const DAY = 86400000, T0 = Date.UTC(2026, 10, 1);
const Q = (id, assessment, skills = ['s']) => ({ id, assessment, skills });
const fresh = () => ({ evidence: [], reviews: {}, quests: {} });

test('aucune preuve : compétence nouvelle', () => {
  assert.equal(skillStatus(fresh(), 's', T0).level, 'nouveau');
});

test('réussite guidée seule : découverte, jamais maîtrise', () => {
  const s = fresh();
  recordAttempt(s, Q('g', 'guided'), { ok: true, assisted: false, at: T0 });
  assert.equal(skillStatus(s, 's', T0).level, 'decouverte');
  assert.equal(Object.keys(s.reviews).length, 0, 'pas de réactivation planifiée sur du guidé');
});

test('réussite autonome mais assistée (indice) : pas une preuve autonome', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: true, at: T0 });
  assert.equal(skillStatus(s, 's', T0).level, 'decouverte');
});

test('une seule réussite autonome : en cours, pas maîtrise ; réactivation à J+1', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 });
  const st = skillStatus(s, 's', T0);
  assert.equal(st.level, 'encours');
  assert.equal(s.reviews.s.due, T0 + DEFAULT_PARAMS.reviewIntervalsDays[0] * DAY);
  assert.ok(st.missing.length >= 2);
});

test('répéter la même quête autonome ne suffit pas (activités distinctes requises)', () => {
  const s = fresh();
  for (let k = 0; k < 5; k++) recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 + k * 2 * DAY });
  assert.equal(skillStatus(s, 's', T0 + 10 * DAY).level, 'encours');
});

test('maîtrise : 2 activités distinctes dont transfert + réussite différée ≥ 1 jour', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 });
  recordAttempt(s, Q('t', 'transfer'), { ok: true, assisted: false, at: T0 + 3600e3 });
  assert.equal(skillStatus(s, 's', T0 + 2 * 3600e3).level, 'encours', 'pas de différé encore');
  recordAttempt(s, Q('r', 'review'), { ok: true, assisted: false, at: T0 + 2 * DAY });
  assert.equal(skillStatus(s, 's', T0 + 2 * DAY).level, 'maitrise');
});

test('sans transfert, pas de maîtrise', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 });
  recordAttempt(s, Q('b', 'autonomous'), { ok: true, assisted: false, at: T0 + 2 * DAY });
  const st = skillStatus(s, 's', T0 + 2 * DAY);
  assert.equal(st.level, 'encours');
  assert.ok(st.missing.some(m => m.includes('transfert')));
});

test('échec isolé en réactivation : maîtrise conservée mais « à consolider », pas de retrait', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 });
  recordAttempt(s, Q('t', 'transfer'), { ok: true, assisted: false, at: T0 + 2 * DAY });
  recordAttempt(s, Q('r', 'review'), { ok: false, assisted: false, at: T0 + 5 * DAY });
  assert.equal(skillStatus(s, 's', T0 + 5 * DAY).level, 'consolider');
  recordAttempt(s, Q('r2', 'review'), { ok: true, assisted: false, at: T0 + 7 * DAY });
  assert.equal(skillStatus(s, 's', T0 + 7 * DAY).level, 'maitrise');
});

test('calendrier : succès en réactivation allonge l\'intervalle, échec le raccourcit', () => {
  const s = fresh(), iv = DEFAULT_PARAMS.reviewIntervalsDays;
  recordAttempt(s, Q('a', 'autonomous'), { ok: true, assisted: false, at: T0 });
  recordAttempt(s, Q('r', 'review'), { ok: true, assisted: false, at: T0 + DAY });
  assert.equal(s.reviews.s.step, 1);
  assert.equal(s.reviews.s.due, T0 + DAY + iv[1] * DAY);
  recordAttempt(s, Q('r', 'review'), { ok: false, assisted: false, at: T0 + 5 * DAY });
  assert.equal(s.reviews.s.step, 0);
  assert.equal(s.reviews.s.due, T0 + 5 * DAY + iv[0] * DAY);
});

test('réactivations dues triées et choix d\'une activité review la moins récente', () => {
  const s = fresh();
  recordAttempt(s, Q('a', 'autonomous', ['x']), { ok: true, assisted: false, at: T0 });
  recordAttempt(s, Q('b', 'autonomous', ['y']), { ok: true, assisted: false, at: T0 + 3600e3 });
  assert.deepEqual(dueSkills(s, T0 + 2 * DAY), ['x', 'y']);
  const quests = [Q('rx1', 'review', ['x']), Q('rx2', 'review', ['x']), Q('a', 'autonomous', ['x'])];
  recordAttempt(s, quests[0], { ok: true, assisted: false, at: T0 + 2 * DAY });
  assert.equal(pickReview(s, quests, 'x').id, 'rx2');
});

test('disponibilité par prérequis et suggestions', () => {
  const s = fresh();
  const quests = [{ id: 'q1', main: true, requires: [], skills: [] }, { id: 'q2', main: true, requires: ['q1'], skills: [] }];
  assert.equal(questAvailable(s, quests[1]), false);
  s.quests.q1 = { status: 'done' };
  assert.equal(questAvailable(s, quests[1]), true);
  assert.deepEqual(suggestions(s, quests, T0).map(x => x.quest.id), ['q2']);
});
