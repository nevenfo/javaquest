// Génère docs/COUVERTURE.md : pour chaque compétence, provenance ESIEE et activités par type d'évaluation.
// Signale les compétences sans parcours complet (guidé → autonome → transfert → réactivation). Usage : node tools/coverage.mjs
import { readFileSync, readdirSync, writeFileSync, existsSync } from 'node:fs';
import { join, resolve } from 'node:path';

const ROOT = resolve(import.meta.dirname, '..');
const skills = JSON.parse(readFileSync(join(ROOT, 'content/skills.json'), 'utf8'));
const qdir = join(ROOT, 'content/quests');
const quests = readdirSync(qdir).filter(d => existsSync(join(qdir, d, 'quest.json'))).map(d => JSON.parse(readFileSync(join(qdir, d, 'quest.json'), 'utf8')));
const STAGES = ['guided', 'practice', 'autonomous', 'transfer', 'review', 'exam'];
const LABEL = { guided: 'guidé', practice: 'entraînement', autonomous: 'autonome', transfer: 'transfert', review: 'réactivation', exam: 'examen' };

let md = `# Couverture pédagogique\n\nGénéré par \`node tools/coverage.mjs\` le ${new Date().toISOString().slice(0, 10)} à partir de \`content/\`. Chaque activité est validée automatiquement (\`Validate\` : solutions de référence acceptées, solutions fautives refusées).\n\n`;
md += `**${quests.length} activités**, **${skills.skills.length} compétences**.\n\n`;
const counts = Object.fromEntries(STAGES.map(s => [s, quests.filter(q => q.assessment === s).length]));
md += '| ' + STAGES.map(s => LABEL[s]).join(' | ') + ' |\n|' + STAGES.map(() => '---').join('|') + '|\n| ' + STAGES.map(s => counts[s]).join(' | ') + ' |\n\n';
md += '| Compétence | Région | Provenance (cours) | Guidé | Autonome | Transfert | Réactivation | Examen | Parcours complet |\n|---|---|---|---|---|---|---|---|---|\n';
let complete = 0;
const gaps = [];
for (const sk of skills.skills) {
  const qs = quests.filter(q => (q.skills || []).includes(sk.id));
  const by = (a) => qs.filter(q => q.assessment === a).map(q => q.id);
  const g = [...by('guided'), ...by('practice')], a = by('autonomous'), t = by('transfer'), r = by('review'), e = by('exam');
  const ok = g.length && a.length && t.length && r.length;
  if (ok) complete++; else gaps.push(sk.id);
  md += `| ${sk.title} (\`${sk.id}\`) | ${sk.region} | ${sk.source || 'complément'} | ${g.length} | ${a.length} | ${t.length} | ${r.length} | ${e.length} | ${ok ? '✔' : '—'} |\n`;
}
md += `\n**Parcours complet** (≥ 1 activité guidée/entraînement, autonome, transfert et réactivation) : ${complete}/${skills.skills.length}.\n`;
if (gaps.length) md += `\nÀ compléter : ${gaps.map(g => '`' + g + '`').join(', ')}.\n`;
md += `\nLes énoncés sont originaux ; le champ « Provenance » cite l'objectif du support ESIEE dont la compétence est issue, sans en reproduire le texte.\n`;
writeFileSync(join(ROOT, 'docs/COUVERTURE.md'), md);
console.log(`${quests.length} activités, parcours complet ${complete}/${skills.skills.length}${gaps.length ? ' ; à compléter : ' + gaps.join(', ') : ''}`);
