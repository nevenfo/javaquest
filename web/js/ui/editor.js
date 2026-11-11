// Atelier de code : éditeur Java (CodeMirror), exécution et vérification réelles, problèmes, pas-à-pas, indices.
import { api } from '../api.js';
import { explainDiagnostic, explainException } from '../errors.js';
import { play } from '../sound.js';
import { md, esc } from './text.js';

const $ = (id) => document.getElementById(id);

export class Workshop {
  constructor(hooks) {
    this.hooks = hooks; // { session, scene(), onPass(quest, info), onFail(quest, info), onClose(), lessonFor(quest), skillTitle(id) }
    this.el = $('editor');
    this.cm = CodeMirror($('ed-code'), {
      mode: 'text/x-java', theme: 'jq', lineNumbers: true, indentUnit: 4, tabSize: 4, indentWithTabs: false, matchBrackets: true,
      styleActiveLine: true, autoCloseBrackets: false, gutters: ['CodeMirror-linenumbers', 'errors'],
      extraKeys: {
        'Ctrl-Enter': () => this.run(), 'Shift-Ctrl-Enter': () => this.check(), 'Ctrl-S': () => this.saveDraft(true),
        Tab: (cm) => cm.somethingSelected() ? cm.indentSelection('add') : cm.replaceSelection('    ', 'end'),
        'Shift-Tab': (cm) => cm.indentSelection('subtract'), Esc: () => this.close(),
      },
    });
    // Trous à compléter « /* ??? */ » mis en évidence (exercices guidés).
    this.cm.addOverlay({ token: (st) => { if (st.match('/* ??? */')) return 'blank'; while (st.next() != null) { if (st.match('/* ???', false)) break; } return null; } });
    this.cm.on('change', () => { this.dirty = true; clearTimeout(this.saveT); this.saveT = setTimeout(() => this.saveDraft(), 800); });
    $('ed-run').onclick = () => this.run();
    $('ed-check').onclick = () => this.check();
    $('ed-reset').onclick = () => this.reset();
    $('ed-close').onclick = () => this.close();
    $('ed-infotabs').onclick = (e) => { const b = e.target.closest('button'); if (b) this.showInfo(b.dataset.info); };
    $('ed-outtabs').onclick = (e) => { const b = e.target.closest('button'); if (b) this.showOut(b.dataset.out); };
    addEventListener('keydown', (e) => {
      if (this.el.hidden) return;
      if (e.ctrlKey && e.key === 'Enter') { e.preventDefault(); e.shiftKey ? this.check() : this.run(); }
      if (e.key === 'Escape' && !e.target.closest?.('.CodeMirror')) this.close();
    });
    this.result = null;
  }

  get open() { return !this.el.hidden; }

  // ---------- Ouverture ----------
  openQuest(quest, opts = {}) {
    this.quest = quest;
    this.exam = opts.exam || null;
    const qs = this.hooks.session.quest(quest.id);
    if (qs.status === 'available') qs.status = 'active';
    qs.openedAt ||= Date.now();
    this.kind = quest.kind || 'code';
    $('ed-title').textContent = quest.title;
    $('ed-kind').textContent = { code: 'Sort à écrire', fix: 'Sort à réparer', parsons: 'Runes à ordonner', predict: 'Prédiction', quiz: 'Questions du sage' }[this.kind] + ' · ' + assessmentLabel(quest.assessment);
    this.files = this.loadFiles();
    this.active = this.files[0]?.name;
    this.renderFiles();
    $('ed-code').hidden = !['code', 'fix'].includes(this.kind);
    $('ed-parsons').hidden = this.kind !== 'parsons';
    $('ed-predict').hidden = !['predict', 'quiz'].includes(this.kind);
    $('ed-files').hidden = !['code', 'fix'].includes(this.kind);
    $('ed-run').hidden = !['code', 'fix', 'parsons'].includes(this.kind);
    $('ed-reset').hidden = this.kind === 'quiz' || this.kind === 'predict';
    document.querySelector('.ed-stdin-label').hidden = !quest.usesStdin;
    $('ed-stdin').value = quest.sampleStdin || '';
    if (this.kind === 'parsons') this.renderParsons();
    if (this.kind === 'predict') this.renderPredict();
    if (this.kind === 'quiz') this.renderQuiz();
    // Mode examen : pas d'indice ni de rappel, « Rendre la copie » au lieu de « Vérifier ».
    [...$('ed-infotabs').children].forEach(b => { b.hidden = !!this.exam && (b.dataset.info === 'hints' || b.dataset.info === 'lesson'); });
    $('ed-check').innerHTML = this.exam ? '📝 Rendre la copie' : '✔ Vérifier la quête <kbd>Ctrl+Maj+Entrée</kbd>';
    let bar = document.getElementById('ed-exam');
    if (!bar) { bar = document.createElement('div'); bar.id = 'ed-exam'; bar.className = 'ed-exam'; this.el.insertBefore(bar, $('ed-infotabs')); }
    bar.hidden = !this.exam;
    if (this.exam) this.exam.renderBar(bar);
    this.el.hidden = false;
    this.hooks.layout?.();
    this.showInfo('brief');
    this.result = null;
    const intro = this.exam ? 'Épreuve : exécute ton code autant que tu veux ; la correction n'a lieu qu'en rendant la copie.'
      : { quiz: 'Coche tes réponses puis « Vérifier la quête ».', predict: 'Écris ta prédiction puis « Vérifier la quête » : le programme sera réellement exécuté pour comparer.',
          parsons: 'Range les runes puis « Exécuter » pour essayer, ou « Vérifier la quête ».' }[this.kind]
      || 'Écris ton code puis « Exécuter » (Ctrl+Entrée) pour l'essayer, ou « Vérifier la quête » (Ctrl+Maj+Entrée) pour la valider avec des tests cachés.';
    this.renderOut('console', `<div class="out-sys">${intro}</div>`);
    this.hooks.scene()?.setupStage(quest.stage);
    play('book');
    setTimeout(() => { this.cm.refresh(); if (!$('ed-code').hidden) this.cm.focus(); }, 30);
  }

  close() {
    if (!this.open) return;
    this.saveDraft();
    this.el.hidden = true;
    this.clearMarks();
    this.hooks.scene()?.clearStage();
    this.hooks.scene()?.followHero();
    this.hooks.layout?.();
    play('bookClose');
    this.hooks.onClose?.();
  }

  loadFiles() {
    const qs = this.hooks.session.quest(this.quest.id);
    const starter = this.quest.starter || { 'Main.java': 'void main() {\n    \n}\n' };
    const saved = qs.drafts && Object.keys(qs.drafts).length ? qs.drafts : starter;
    const files = Object.entries(saved).map(([name, content]) => ({ name, doc: CodeMirror.Doc(content, 'text/x-java') }));
    for (const s of this.quest.support || []) files.push({ name: s.name, doc: CodeMirror.Doc(s.content, 'text/x-java'), readOnly: true });
    return files;
  }

  renderFiles() {
    const bar = $('ed-files');
    bar.innerHTML = '';
    for (const f of this.files) {
      const b = document.createElement('button');
      b.textContent = f.name + (f.readOnly ? ' 🔒' : '');
      b.className = (f.name === this.active ? 'on ' : '') + (f.readOnly ? 'ro' : '');
      b.title = f.readOnly ? 'Fichier fourni par la quête (lecture seule) : lis-le pour connaître les méthodes disponibles.' : 'Ton fichier';
      b.onclick = () => this.select(f.name);
      if (!f.readOnly && this.quest.multiFile && this.files.filter(x => !x.readOnly).length > 1) {
        b.oncontextmenu = (e) => { e.preventDefault(); if (confirm(`Supprimer ${f.name} ?`)) { this.files = this.files.filter(x => x !== f); this.active = this.files[0].name; this.renderFiles(); this.saveDraft(); } };
      }
      bar.appendChild(b);
    }
    if (this.quest.multiFile) {
      const add = document.createElement('button');
      add.className = 'add'; add.textContent = '+ Fichier';
      add.title = 'Ajouter une classe (un fichier par classe publique)';
      add.onclick = () => {
        const n = prompt('Nom de la classe (ex. Point) :');
        if (!n || !/^[A-Z][A-Za-z0-9_]*$/.test(n)) return n && alert('Nom de classe Java invalide : commence par une majuscule, lettres/chiffres uniquement.');
        if (this.files.some(f => f.name === n + '.java')) return;
        this.files.splice(this.files.filter(f => !f.readOnly).length, 0, { name: n + '.java', doc: CodeMirror.Doc(`public class ${n} {\n    \n}\n`, 'text/x-java') });
        this.select(n + '.java');
        this.saveDraft();
      };
      bar.appendChild(add);
    }
    const cur = this.files.find(f => f.name === this.active);
    if (cur) { this.cm.swapDoc(cur.doc); this.cm.setOption('readOnly', !!cur.readOnly); }
  }

  select(name) { this.active = name; this.renderFiles(); this.cm.focus(); }

  playerFiles() {
    if (this.kind === 'parsons') return [{ name: 'Main.java', content: this.parsonsCode() }];
    return this.files.filter(f => !f.readOnly).map(f => ({ name: f.name, content: f.doc.getValue() }));
  }

  saveDraft(explicit = false) {
    if (!this.quest || !['code', 'fix'].includes(this.kind)) return;
    const qs = this.hooks.session.quest(this.quest.id);
    qs.drafts = Object.fromEntries(this.playerFiles().map(f => [f.name, f.content]));
    this.hooks.session.touch();
    if (explicit) this.hooks.toast?.('Brouillon sauvegardé.', 'good');
  }

  reset() {
    if (!confirm('Revenir au code de départ ? Ton code actuel sera remplacé.')) return;
    const qs = this.hooks.session.quest(this.quest.id);
    qs.drafts = null;
    if (this.kind === 'parsons') { qs.parsons = null; this.renderParsons(); return; }
    this.files = this.loadFiles();
    this.active = this.files[0].name;
    this.renderFiles();
    this.clearMarks();
    this.hooks.scene()?.resetStage();
  }

  // ---------- Panneau d'informations ----------
  showInfo(tab) {
    [...$('ed-infotabs').children].forEach(b => b.classList.toggle('on', b.dataset.info === tab));
    const q = this.quest, qs = this.hooks.session.quest(q.id), box = $('ed-info');
    const hints = q.hints || [];
    $('ed-hintcount').textContent = hints.length ? `(${qs.hintsUsed || 0}/${hints.length})` : '';
    if (tab === 'brief') {
      const skills = (q.skills || []).map(s => `<span class="tag">${esc(this.hooks.skillTitle(s))}</span>`).join('');
      box.innerHTML = md(q.brief || '') + `<p class="muted">${skills}</p>` + (q.source ? `<p class="warn">Lien au cours : ${esc(q.source)}</p>` : '');
    } else if (tab === 'lesson') {
      box.innerHTML = this.hooks.lessonFor(q) || '<p>Pas de rappel spécifique : consulte le Grimoire (G).</p>';
    } else if (tab === 'hints') {
      const used = qs.hintsUsed || 0;
      let h = hints.slice(0, used).map((t, i) => `<div class="hint"><b>Indice ${i + 1}.</b> ${md(t)}</div>`).join('');
      if (used < hints.length) h += `<p><button class="btn" id="ed-hint-more">Révéler l'indice ${used + 1}</button></p><p class="warn">Un indice t'aide maintenant ; la réussite comptera comme « assistée » (récompense identique, mais pas de preuve de maîtrise autonome). Tu pourras la prouver plus tard sur une autre quête ou au tableau des primes.</p>`;
      else if (!hints.length) h = '<p>Pas d\'indice pour cette épreuve.</p>';
      else h += '<p class="muted">Tous les indices sont révélés.</p>';
      box.innerHTML = h;
      const more = $('ed-hint-more');
      if (more) more.onclick = () => { qs.hintsUsed = used + 1; qs.assistedSince = qs.assistedSince || Date.now(); this.hooks.session.touch(); play('page'); this.showInfo('hints'); };
    } else if (tab === 'api') {
      const sup = q.support || [];
      box.innerHTML = sup.length ? '<p>Ces classes sont fournies par la quête ; ouvre leurs onglets 🔒 dans l\'éditeur pour lire leur code.</p>' + sup.map(s => `<p><b>${esc(s.name)}</b></p><pre>${esc(signatures(s.content))}</pre>`).join('')
        : '<p>Aucune classe fournie : tout est à écrire avec Java standard (et <code>Sonde.voir("nom", valeur)</code> pour observer une valeur pendant l\'exécution).</p>';
    }
  }

  // ---------- Exécution ----------
  async run() {
    if (!this.quest || $('ed-run').disabled || $('ed-run').hidden) return;
    this.saveDraft();
    this.busy(true);
    this.renderOut('console', '<div class="out-sys">Compilation et exécution dans la sandbox…</div>');
    const s = this.hooks.session.state; s.stats.runs++;
    try {
      const r = await api.run(this.quest.id, this.playerFiles(), $('ed-stdin').value ? $('ed-stdin').value.replace(/\\n/g, '\n') + '\n' : '', []);
      this.result = r;
      this.showResult(r, false);
      this.busy(false);
      if (r.timeline?.length && !r.compile?.some(c => !c.ok)) this.playTimeline(r.timeline);
    } catch (e) {
      this.renderOut('console', `<div class="out-err">${esc(e.message)}</div>`);
    } finally { this.busy(false); }
  }

  async check() {
    if (!this.quest || $('ed-check').disabled) return;
    if (this.playing) this.playing.cancel = true;
    if (this.exam) { this.saveDraft(); return this.exam.submit(); }
    this.saveDraft();
    this.busy(true);
    const s = this.hooks.session.state; s.stats.checks++;
    const qs = this.hooks.session.quest(this.quest.id);
    qs.checks = (qs.checks || 0) + 1;
    this.renderOut('tests', '<div class="out-sys">Vérification avec les tests cachés…</div>');
    try {
      const extra = this.kind === 'predict' ? { prediction: $('ed-pred').value } : this.kind === 'quiz' ? { answers: this.quizAnswers() } : {};
      const r = await api.check(this.quest.id, this.kind === 'quiz' || this.kind === 'predict' ? [{ name: 'Main.java', content: '' }] : this.playerFiles(), extra);
      this.result = r;
      this.showResult(r, true);
      const assisted = (qs.hintsUsed || 0) > 0 || !!qs.solutionShown;
      if (r.passed) { play('ok'); await this.hooks.onPass(this.quest, { assisted, result: r }); }
      else { play('error'); this.hooks.onFail(this.quest, { assisted, result: r }); }
    } catch (e) {
      this.renderOut('tests', `<div class="out-err">${esc(e.message)}</div>`);
    } finally { this.busy(false); }
  }

  busy(on) { $('ed-run').disabled = on; $('ed-check').disabled = on; }

  showResult(r, isCheck) {
    this.clearMarks();
    const diags = [];
    for (const c of r.compile || []) for (const d of c.diagnostics || []) diags.push({ ...d, phase: c.phase });
    const playerDiags = diags.filter(d => d.phase === 'player');
    const errors = playerDiags.filter(d => d.kind === 'ERROR');
    $('ed-probcount').textContent = errors.length ? `(${errors.length})` : '';
    for (const d of errors) this.markLine(d.file, d.line, 'error');
    let probs = playerDiags.map((d) => this.diagHtml(d)).join('');
    const harnessFail = (r.compile || []).find(c => c.phase === 'harness' && !c.ok);
    if (harnessFail) probs += `<div class="diag"><div class="where">Structure attendue</div><div class="explain">Ton code compile, mais il ne fournit pas ce que la quête attend (nom de classe, de méthode ou signature exacts). Relis la mission.</div><div class="real">${esc((harnessFail.diagnostics || []).map(d => d.message).join('\n').replace(/\n\s*location:.*$/gm, ''))}</div></div>`;
    if (r.exception) {
      const ex = r.exception, f = (ex.frames || []).find(x => x.player);
      if (f) this.markLine(f.file, f.line, 'error');
      const why = explainException(ex.simpleType);
      probs += `<div class="diag" data-file="${esc(f?.file || '')}" data-line="${f?.line || 0}"><div class="where">${f ? `${esc(f.file)} ligne ${f.line}` : 'Exécution'} — exception</div><div class="real">${esc(ex.type)}${ex.message ? ': ' + esc(ex.message) : ''}${(ex.frames || []).map(x => `\n    at ${x.class}.${x.method}(${x.file}:${x.line})`).join('')}</div>${why ? `<div class="explain">💡 ${esc(why)}</div>` : ''}</div>`;
    }
    this.problemsHtml = probs || '<div class="out-ok">Aucun problème détecté.</div>';
    // Console
    let out = '';
    const status = { 'compile-error': '<div class="out-err">✘ Le code ne compile pas : voir l\'onglet Problèmes (clic = aller à la ligne).</div>',
      timeout: '<div class="out-warn">⏱ Arrêté : temps limite dépassé (boucle infinie ?). La sortie ci-dessus est partielle.</div>',
      memory: '<div class="out-warn">Arrêté : limite de mémoire atteinte.</div>', 'output-limit': '<div class="out-warn">Arrêté : trop de texte affiché.</div>',
      exception: '<div class="out-err">✘ Le programme s\'est arrêté sur une exception (onglet Problèmes).</div>',
      'sandbox-unavailable': `<div class="out-err">Exécution impossible : ${esc(r.error || '')}</div>`, error: `<div class="out-err">${esc(r.error || 'Erreur')}</div>`,
      crash: `<div class="out-err">${esc(r.error || 'La JVM s\'est arrêtée.')}</div>`, busy: '<div class="out-warn">Une autre exécution est en cours, réessaie.</div>' }[r.status] || '';
    for (const e of r.timeline || []) if (e.t === 'out') out += `<span class="${e.s === 'err' ? 'out-err' : e.s === 'in' ? 'out-in' : ''}">${esc(e.text)}</span>`;
    if (r.truncated) out += `<div class="out-warn">(sortie tronquée : ${esc(r.truncated)})</div>`;
    if (!isCheck) {
      const ms = r.limits?.wallMs;
      this.renderOut('console', (out ? `<pre style="margin:0;white-space:pre-wrap">${out}</pre>` : (r.status === 'ok' ? '<div class="out-sys">(rien n\'a été affiché)</div>' : '')) + status + (r.status === 'ok' ? `<div class="out-sys">✔ Terminé${ms ? ` en ${ms} ms` : ''}.</div>` : ''));
      if (errors.length || r.exception) this.showOut('problems');
    } else {
      const tests = r.tests || [];
      let h = tests.map(t => `<div class="test-row ${t.ok ? 'ok' : 'ko'}">${esc(t.name)}${t.message ? `<div class="msg">${esc(t.message)}</div>` : ''}</div>`).join('');
      if (!tests.length) h = status || '<div class="out-err">Aucun test n\'a pu s\'exécuter.</div>';
      else if (status && r.status !== 'ok') h += status;
      h += r.passed ? '<div class="out-ok" style="margin-top:8px">✔ Quête validée !</div>' : '<div class="out-warn" style="margin-top:8px">Pas encore : lis les messages ci-dessus, corrige, puis vérifie à nouveau.</div>';
      this.renderOut('tests', h);
      if (errors.length || harnessFail) this.showOut('problems');
    }
    this.traceData = (r.timeline || []).filter(e => e.line);
  }

  diagHtml(d) {
    const why = explainDiagnostic(d);
    return `<div class="diag" data-file="${esc(d.file)}" data-line="${d.line}"><div class="where">${esc(d.file)} ligne ${d.line}, colonne ${d.column} — ${d.kind === 'ERROR' ? 'erreur' : 'avertissement'}</div><div class="real">${esc(d.message)}</div>${why ? `<div class="explain">💡 ${esc(why)}</div>` : ''}</div>`;
  }

  markLine(file, line, kind) {
    const f = this.files.find(x => x.name === file);
    if (!f || !line) return;
    const doc = f.doc, l = Math.min(doc.lineCount(), line) - 1;
    doc.addLineClass(l, 'background', 'line-error');
    (this.marks ||= []).push({ doc, l, cls: 'line-error' });
    if (file === this.active) {
      const m = document.createElement('div'); m.className = 'gutter-error'; m.textContent = '●';
      this.cm.setGutterMarker(l, 'errors', m);
    }
  }

  clearMarks() {
    for (const m of this.marks || []) m.doc.removeLineClass(m.l, 'background', m.cls);
    this.marks = [];
    this.cm.clearGutter('errors');
  }

  goto(file, line) {
    if (!file || !line) return;
    if (this.files.some(f => f.name === file)) this.select(file);
    this.cm.setCursor({ line: line - 1, ch: 0 });
    this.cm.scrollIntoView({ line: line - 1, ch: 0 }, 80);
    this.cm.focus();
  }

  // ---------- Sorties ----------
  showOut(tab) {
    [...$('ed-outtabs').children].forEach(b => b.classList.toggle('on', b.dataset.out === tab));
    this.outTab = tab;
    const box = $('ed-out');
    if (tab === 'problems') box.innerHTML = this.problemsHtml || '<div class="out-sys">Rien à signaler pour l\'instant.</div>';
    else if (tab === 'trace') this.renderTrace();
    else box.innerHTML = (tab === 'console' ? this.consoleHtml : this.testsHtml) || '';
    box.querySelectorAll('.diag[data-line]').forEach(el => el.onclick = () => this.goto(el.dataset.file, +el.dataset.line));
  }

  renderOut(tab, html) {
    if (tab === 'console') this.consoleHtml = html; else this.testsHtml = html;
    this.showOut(tab);
  }

  renderTrace() {
    const box = $('ed-out'), data = this.traceData || [];
    if (!data.length) { box.innerHTML = '<div class="out-sys">Exécute ton code : chaque affichage, valeur observée (Sonde.voir) et action dans le monde apparaîtra ici, dans l\'ordre, avec sa ligne. Ce n\'est pas un débogueur complet : seules ces étapes sont enregistrées.</div>'; return; }
    box.innerHTML = `<div class="trace-ctrl"><button id="tr-replay">▶ Rejouer dans le monde</button><span class="out-sys">${data.length} étape(s) enregistrée(s) — clic sur une ligne pour la voir dans le code.</span></div>` +
      data.map((e, i) => `<div class="trace-row" data-i="${i}"><span class="ln">L${e.line}</span><span class="k">${{ out: 'affiche', watch: 'observe', event: 'action', exception: 'exception' }[e.t] || e.t}</span><span>${esc(traceText(e))}</span></div>`).join('');
    box.querySelectorAll('.trace-row').forEach(el => el.onclick = () => { const e = data[+el.dataset.i]; this.highlight(e.file, e.line); box.querySelectorAll('.trace-row').forEach(x => x.classList.toggle('on', x === el)); });
    $('tr-replay').onclick = () => this.playTimeline(this.result?.timeline || []);
  }

  highlight(file, line) {
    if (this.execMark) this.execMark.doc.removeLineClass(this.execMark.l, 'background', 'line-exec');
    const f = this.files.find(x => x.name === file);
    if (!f || !line) return;
    if (this.active !== file) this.select(file);
    const l = line - 1;
    f.doc.addLineClass(l, 'background', 'line-exec');
    this.execMark = { doc: f.doc, l };
    this.cm.scrollIntoView({ line: l, ch: 0 }, 60);
  }

  /** Rejoue la trace : surligne chaque ligne et anime le monde (sorties = paroles du héros, actions = acteurs). */
  async playTimeline(timeline) {
    const scene = this.hooks.scene();
    if (!scene) return;
    if (this.playing) { this.playing.cancel = true; }
    const token = { cancel: false };
    this.playing = token;
    scene.resetStage();
    const speed = this.hooks.session.state.settings.speed || 1;
    let shown = 0;
    for (const e of timeline) {
      if (token.cancel || !this.open) break;
      if (++shown > 400) break; // rejouer au plus 400 étapes
      this.highlight(e.file, e.line);
      if (e.t === 'event') await scene.playEvent(e, speed);
      else if (e.t === 'out' && this.quest.stage?.speak !== false) await scene.playEvent({ name: 'heros.dire', args: [e.text.replace(/\n$/, '')] }, speed * 1.4);
      else if (e.t === 'watch') { scene.bubble(scene.hero, `${e.label} = ${e.value}`, 1600 / speed, 'watch'); await wait(260 / speed); }
      else if (e.t === 'exception') { scene.effect('fumee', Math.floor(scene.hero.x / 16), Math.floor(scene.hero.y / 16) - 1); await wait(300); }
    }
    if (this.execMark && !token.cancel) setTimeout(() => { if (this.execMark) { this.execMark.doc.removeLineClass(this.execMark.l, 'background', 'line-exec'); this.execMark = null; } }, 1200);
  }

  // ---------- Runes à ordonner (Parsons) ----------
  renderParsons() {
    const q = this.quest, qs = this.hooks.session.quest(q.id);
    const all = [...q.parsons.lines, ...(q.parsons.distractors || [])].map((t, i) => ({ id: i, text: t.trim() }));
    if (!qs.parsons) {
      const pool = all.map(l => l.id).sort((a, b) => ((a * 7919 + 13) % 101) - ((b * 7919 + 13) % 101));
      qs.parsons = { pool, placed: [] };
    }
    const box = $('ed-parsons');
    const line = (id, indent, where) => `<div class="rune" tabindex="0" draggable="true" data-id="${id}" data-where="${where}">${where === 'placed' ? `<span class="ind">${'·   '.repeat(indent)}</span>` : ''}${esc(all[id].text)}</div>`;
    box.innerHTML = `<div class="col" data-col="pool"><h4>Runes disponibles</h4>${qs.parsons.pool.map(id => line(id, 0, 'pool')).join('')}</div>
      <div class="col" data-col="placed"><h4>Ton sort (ordre + indentation)</h4>${qs.parsons.placed.map(p => line(p.id, p.indent, 'placed')).join('')}</div>
      <p class="out-sys" style="grid-column:1/3;margin:0">Glisse les runes à droite, ou : Entrée = déplacer, ↑/↓ = monter/descendre, Tab / Maj+Tab = indenter. Certaines runes sont des pièges.</p>`;
    const move = (id, toPlaced, index = null) => {
      qs.parsons.pool = qs.parsons.pool.filter(x => x !== id);
      const prev = qs.parsons.placed.find(p => p.id === id);
      qs.parsons.placed = qs.parsons.placed.filter(p => p.id !== id);
      if (toPlaced) qs.parsons.placed.splice(index ?? qs.parsons.placed.length, 0, { id, indent: prev?.indent ?? 1 });
      else qs.parsons.pool.push(id);
      this.hooks.session.touch(); this.renderParsons(); play('tick');
    };
    box.querySelectorAll('.rune').forEach(el => {
      const id = +el.dataset.id, placed = el.dataset.where === 'placed';
      el.ondragstart = (e) => e.dataTransfer.setData('text/plain', String(id));
      el.onkeydown = (e) => {
        const arr = qs.parsons.placed, i = arr.findIndex(p => p.id === id);
        if (e.key === 'Enter') { e.preventDefault(); move(id, !placed); }
        else if (placed && e.key === 'Tab') { e.preventDefault(); arr[i].indent = Math.max(0, Math.min(4, arr[i].indent + (e.shiftKey ? -1 : 1))); this.renderParsons(); focusRune(id); }
        else if (placed && (e.key === 'ArrowUp' || e.key === 'ArrowDown')) { e.preventDefault(); const j = i + (e.key === 'ArrowUp' ? -1 : 1); if (j >= 0 && j < arr.length) { [arr[i], arr[j]] = [arr[j], arr[i]]; this.renderParsons(); focusRune(id); } }
      };
    });
    const focusRune = (id) => box.querySelector(`.rune[data-id="${id}"]`)?.focus();
    box.querySelectorAll('.col').forEach(col => {
      col.ondragover = (e) => e.preventDefault();
      col.ondrop = (e) => {
        e.preventDefault();
        const id = +e.dataTransfer.getData('text/plain');
        const toPlaced = col.dataset.col === 'placed';
        let index = null;
        if (toPlaced) { const runes = [...col.querySelectorAll('.rune')]; index = runes.findIndex(r => e.clientY < r.getBoundingClientRect().top + r.offsetHeight / 2); if (index < 0) index = null; }
        move(id, toPlaced, index);
      };
    });
    this.parsonsAll = all;
  }

  parsonsCode() {
    const qs = this.hooks.session.quest(this.quest.id), all = this.parsonsAll || [];
    const body = qs.parsons.placed.map(p => '    '.repeat(p.indent) + all[p.id].text).join('\n');
    return (this.quest.parsons.prefix || '') + body + '\n' + (this.quest.parsons.suffix || '');
  }

  // ---------- Prédiction / questions ----------
  renderPredict() {
    const q = this.quest, prog = (q.program || []).map(f => `<div class="out-sys">${esc(f.name)}</div><pre>${esc(f.content)}</pre>`).join('');
    $('ed-predict').innerHTML = `${prog}<p style="color:#e8e3f7">Qu'affichera exactement ce programme ? Écris chaque ligne de sortie :</p><textarea id="ed-pred" spellcheck="false" placeholder="ligne 1&#10;ligne 2"></textarea>`;
    const qs = this.hooks.session.quest(q.id);
    $('ed-pred').value = qs.prediction || '';
    $('ed-pred').oninput = () => { qs.prediction = $('ed-pred').value; this.hooks.session.touch(); };
  }

  renderQuiz() {
    const q = this.quest, qs = this.hooks.session.quest(q.id), saved = qs.quizAnswers || [];
    $('ed-predict').innerHTML = (q.questions || []).map((qq, i) => `<div class="card" style="background:#24203a;border-color:#443b63;color:#e8e3f7;padding:8px 12px;border-radius:8px;margin:8px 0">
      <p><b>${i + 1}.</b> ${md(qq.q)} <span class="out-sys">${qq.multi ? '(une ou plusieurs réponses)' : '(une seule réponse)'}</span></p>
      ${qq.options.map((o, j) => `<label style="display:block;margin:3px 0"><input type="${qq.multi ? 'checkbox' : 'radio'}" name="qz${i}" value="${j}" ${saved[i]?.includes(j) ? 'checked' : ''}> ${md(o, true)}</label>`).join('')}</div>`).join('');
    $('ed-predict').onchange = () => { qs.quizAnswers = this.readQuiz(); this.hooks.session.touch(); };
  }

  readQuiz() {
    return (this.quest.questions || []).map((qq, i) => [...document.querySelectorAll(`input[name="qz${i}"]:checked`)].map(x => +x.value));
  }

  quizAnswers() { return this.hooks.session.quest(this.quest.id).quizAnswers || this.readQuiz(); }
}

function wait(ms) { return new Promise(r => setTimeout(r, ms)); }

function traceText(e) {
  if (e.t === 'out') return e.text.replace(/\n$/, '⏎');
  if (e.t === 'watch') return `${e.label} = ${e.value}  (${e.type})`;
  if (e.t === 'event') return `${e.name}(${(e.args || []).map(a => JSON.stringify(a)).join(', ')})`;
  if (e.t === 'exception') return e.simpleType;
  return '';
}

function assessmentLabel(a) {
  return { worked: 'exemple commenté', guided: 'guidé', practice: 'entraînement', autonomous: 'autonome', transfer: 'transfert', review: 'réactivation', exam: 'examen' }[a] || 'entraînement';
}

/** Signatures publiques d'un fichier fourni (aperçu de l'API). */
function signatures(src) {
  return src.split('\n').filter(l => /^\s*(public|static|record|class|interface|enum|void|int|double|boolean|String|[A-Z]\w*\s+\w+\s*\()/.test(l) && !/^\s*(\/\/|\*|\/\*)/.test(l) && !/Trace\./.test(l))
    .map(l => l.replace(/\{\s*$/, '').replace(/\s+$/, '')).join('\n');
}
