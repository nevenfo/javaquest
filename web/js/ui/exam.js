// Citadelle des Épreuves : épreuves blanches chronométrées (aides désactivées, copie rendue en fin d'épreuve)
// et consultation verbatim d'un sujet officiel fourni par l'étudiant (jamais distribué avec le jeu).
import { md, esc } from './text.js';
import { recordAttempt, skillStatus, LEVELS } from '../pedagogy.js';

/** Épreuves blanches originales (parties = quêtes « exam » de content/quests). */
export const EXAMS = [
  { id: 'blanc-1', title: 'Épreuve blanche n°1', minutes: 90, parts: ['e01-questions', 'e02-chaudron', 'e03-runes', 'e04-creatures'],
    about: 'Format proche du contrôle d\'octobre : questions de cours, une classe avec exceptions, un traitement de données avec collections, une modélisation par interfaces. Sujets originaux, corrigés automatiquement.' },
];

export class ExamHall {
  constructor({ G, modal, toast, api, play }) { Object.assign(this, { G, modal, toast, api, play }); this.timer = null; }

  get state() { return this.G.session.state; }

  enter() { this.G.scene.loadMap('citadelle', { x: 17, y: 23 }); }

  menu() {
    const s = this.state, active = s.exam && !s.exam.done ? s.exam : null;
    const hist = (s.examHistory || []).slice(-6).reverse();
    const rows = EXAMS.map(e => {
      const avail = e.parts.every(id => this.G.byId.has(id));
      return `<div class="card"><b>${esc(e.title)}</b> <span class="tag">${e.minutes} min</span> <span class="tag">${e.parts.length} parties</span><div class="muted">${esc(e.about)}</div>
        <div class="row" style="margin-top:6px">${avail ? `<button class="btn primary" data-start="${e.id}" ${active ? 'disabled' : ''}>Commencer</button>` : '<span class="muted">Parties manquantes dans ce contenu.</span>'}</div></div>`;
    }).join('');
    const official = s.officialSubjects?.annale;
    const { box, close } = this.modal(`<h1>🏛 Pupitre des épreuves</h1>
      <p class="muted">Conditions d'épreuve : chronomètre, ni indice ni rappel de cours, tu peux exécuter ton code (comme sur ton poste), la copie n'est corrigée qu'à la fin. Les réussites comptent comme preuves <b>autonomes</b> de type examen.</p>
      ${active ? `<div class="card"><b>Épreuve en cours : ${esc(EXAMS.find(e => e.id === active.id)?.title || active.id)}</b> — reste ${fmt(active.end - Date.now())} <button class="btn primary" id="ex-resume">Reprendre</button></div>` : ''}
      <h2>Épreuves blanches</h2>${rows}
      <h2>Sujet officiel (annale)</h2>
      <div class="card">Les sujets ESIEE ne sont pas inclus dans JavaQuest (droits des enseignants). Si tu disposes du sujet officiel (fichier texte téléchargé depuis e-learning), importe-le : il sera affiché <b>sans aucune modification</b>, uniquement sur ce PC, avec un espace de travail pour le traiter en conditions réelles.
        <div class="row" style="margin-top:6px"><label class="btn">📄 Importer le sujet (.txt)<input type="file" id="ex-import" accept=".txt,text/plain" hidden></label>
        ${official ? `<button class="btn primary" id="ex-official">Traiter le sujet importé (${official.text.length} caractères)</button><button class="btn ghost" id="ex-forget">Retirer</button>` : ''}</div>
        <div class="muted">Correction automatique non proposée pour le sujet officiel : l'énoncé ne fixe pas toutes les signatures, et le jeu ne le réécrit pas. Une grille d'auto-évaluation est proposée à la fin.</div></div>
      <h2>Historique</h2>${hist.map(h => `<div class="muted">${new Date(h.at).toLocaleString('fr-FR')} — ${esc(h.title)} : ${h.score}/${h.total} tests (${h.minutes} min)</div>`).join('') || '<p class="muted">Aucune épreuve passée.</p>'}
      <p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
    box.querySelectorAll('[data-start]').forEach(b => b.onclick = () => { close(); this.start(EXAMS.find(e => e.id === b.dataset.start)); });
    box.querySelector('#ex-resume')?.addEventListener('click', () => { close(); this.openPart(active.current || 0); });
    box.querySelector('#ex-import').onchange = async (e) => {
      const f = e.target.files[0]; if (!f) return;
      const text = await f.text();
      s.officialSubjects ||= {}; s.officialSubjects.annale = { name: f.name, text, importedAt: Date.now() };
      this.G.session.touch(); close(); this.menu(); this.toast('Sujet importé (conservé sur ce PC uniquement).', 'good');
    };
    box.querySelector('#ex-official')?.addEventListener('click', () => { close(); this.official(); });
    box.querySelector('#ex-forget')?.addEventListener('click', () => { delete s.officialSubjects.annale; this.G.session.touch(); close(); this.menu(); });
  }

  start(exam) {
    const s = this.state;
    for (const id of exam.parts) { const qs = this.G.session.quest(id); qs.drafts = null; qs.quizAnswers = null; qs.prediction = null; qs.parsons = null; }
    s.exam = { id: exam.id, start: Date.now(), end: Date.now() + exam.minutes * 60000, current: 0, done: false };
    this.G.session.touch();
    this.play('magic');
    this.openPart(0);
  }

  openPart(i) {
    const s = this.state, exam = EXAMS.find(e => e.id === s.exam.id);
    s.exam.current = i;
    const q = this.G.byId.get(exam.parts[i]);
    this.G.scene.freeze(true);
    this.G.workshop.openQuest(q, { exam: {
      renderBar: (bar) => {
        bar.innerHTML = `<b>${esc(exam.title)}</b> ` + exam.parts.map((id, k) => `<button data-part="${k}" class="${k === i ? 'on' : ''}">Partie ${k + 1}</button>`).join('') + '<span class="timer" id="ex-timer"></span>';
        bar.querySelectorAll('[data-part]').forEach(b => b.onclick = () => { this.G.workshop.saveDraft(); this.openPart(+b.dataset.part); });
        this.tick();
      },
      submit: () => this.submit(),
    } });
    clearInterval(this.timer);
    this.timer = setInterval(() => this.tick(), 1000);
  }

  tick() {
    const s = this.state;
    if (!s.exam || s.exam.done) { clearInterval(this.timer); return; }
    const left = s.exam.end - Date.now(), el = document.getElementById('ex-timer');
    if (el) { el.textContent = '⏳ ' + fmt(left); el.classList.toggle('low', left < 5 * 60000); }
    if (left <= 0) { clearInterval(this.timer); this.toast('Temps écoulé : la copie est rendue.', 'bad'); this.submit(true); }
  }

  async submit(auto = false) {
    const s = this.state, exam = EXAMS.find(e => e.id === s.exam?.id);
    if (!exam || s.exam.done) return;
    if (!auto && !confirm('Rendre la copie maintenant ? Tu ne pourras plus la modifier.')) return;
    s.exam.done = true;
    clearInterval(this.timer);
    this.G.workshop.close();
    const results = [];
    let score = 0, total = 0;
    for (const id of exam.parts) {
      const q = this.G.byId.get(id), qs = this.G.session.quest(id);
      let r;
      try {
        const files = q.kind === 'quiz' || q.kind === 'predict' ? [{ name: 'Main.java', content: '' }]
          : Object.entries(qs.drafts || q.starter || {}).map(([name, content]) => ({ name, content }));
        r = await this.api.check(id, files, q.kind === 'quiz' ? { answers: qs.quizAnswers || [] } : q.kind === 'predict' ? { prediction: qs.prediction || '' } : {});
      } catch (e) { r = { passed: false, tests: [], error: e.message }; }
      const tests = r.tests || [], ok = tests.filter(t => t.ok).length;
      score += ok; total += Math.max(tests.length, 1);
      recordAttempt(s, { ...q, assessment: 'exam' }, { ok: !!r.passed, assisted: false });
      results.push({ q, r, ok, n: tests.length });
    }
    const minutes = Math.round((Date.now() - s.exam.start) / 60000);
    (s.examHistory ||= []).push({ at: Date.now(), id: exam.id, title: exam.title, score, total, minutes });
    this.G.session.touch();
    const skills = [...new Set(exam.parts.flatMap(id => this.G.byId.get(id).skills || []))];
    this.play(score === total ? 'ok' : 'page');
    this.modal(`<h1>📝 Résultats — ${esc(exam.title)}</h1><p><b>${score} / ${total}</b> tests réussis en ${minutes} min.</p>
      ${results.map((x, k) => `<div class="card"><b>Partie ${k + 1} — ${esc(x.q.title)}</b> <span class="tag ${x.r.passed ? 'lvl-maitrise' : 'lvl-consolider'}">${x.ok}/${x.n}</span>
        ${(x.r.tests || []).map(t => `<div class="muted">${t.ok ? '✔' : '✘'} ${esc(t.name)}${!t.ok && t.message ? ' — ' + esc(t.message) : ''}</div>`).join('')}${x.r.error ? `<div class="muted">${esc(x.r.error)}</div>` : ''}</div>`).join('')}
      <h2>Compétences mobilisées</h2>${skills.map(k => { const st = skillStatus(s, k, Date.now(), this.G.params); return `<span class="tag ${LEVELS[st.level].css}">${esc(this.G.skills.get(k)?.title || k)} : ${LEVELS[st.level].label}</span>`; }).join(' ')}
      <p class="muted">Conseil : retravaille d'abord les parties échouées au tableau des primes, puis repasse une épreuve dans quelques jours.</p>
      <p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
  }

  official() {
    const sub = this.state.officialSubjects.annale;
    const quest = { id: 'officiel-annale', title: 'Sujet officiel (importé)', kind: 'code', assessment: 'exam', multiFile: true, skills: [],
      brief: 'L\'énoncé officiel est affiché **tel quel** dans la fenêtre « Énoncé » (bouton ci-dessous). Crée tes classes avec « + Fichier ». Exécute avec Ctrl+Entrée.',
      starter: { 'Main.java': 'void main() {\n    \n}\n' }, support: [] };
    const showSubject = () => this.modal(`<h1>📄 ${esc(sub.name)}</h1><p class="muted">Texte importé le ${new Date(sub.importedAt).toLocaleString('fr-FR')}, affiché sans modification.</p><pre style="white-space:pre-wrap;background:#fffaf0;color:#2a1d16">${esc(sub.text)}</pre><p><button class="btn" data-close>Fermer</button></p>`, { wide: true });
    this.G.scene.freeze(true);
    this.G.workshop.openQuest(quest, { exam: {
      renderBar: (bar) => { bar.innerHTML = '<b>Sujet officiel</b> <button id="ex-subject">📄 Énoncé</button><span class="timer">sans correction automatique</span>'; bar.querySelector('#ex-subject').onclick = showSubject; },
      submit: () => {
        this.G.workshop.close();
        this.modal(`<h1>Auto-évaluation</h1><p>Relis l'énoncé officiel et coche honnêtement ce que ton code réalise. Cette grille n'est pas une note et ne compte pas comme preuve de maîtrise.</p>
          ${['Toutes les classes demandées existent avec les noms et paquetages imposés', 'Chaque méthode imposée a la signature exacte de l\'énoncé', 'Les cas d\'erreur demandés lèvent une exception', 'equals / hashCode / toString sont cohérents', 'J\'ai testé chaque fonctionnalité avec un main', 'Le code compile sans avertissement'].map(t => `<label style="display:block"><input type="checkbox"> ${esc(t)}</label>`).join('')}
          <p><button class="btn" data-close>Terminer</button></p>`);
      },
    } });
    showSubject();
  }
}

function fmt(ms) { ms = Math.max(0, ms); const m = Math.floor(ms / 60000), sec = Math.floor(ms % 60000 / 1000); return `${m}:${String(sec).padStart(2, '0')}`; }
