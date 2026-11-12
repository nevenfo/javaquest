// Mise en forme minimale et sûre (échappement systématique) : `code`, **gras**, blocs ``` et paragraphes.
export function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

export function md(src, inline = false) {
  const parts = String(src ?? '').split(/```(?:java)?\n?/);
  let html = '';
  parts.forEach((p, i) => {
    if (i % 2 === 1) { html += `<pre>${esc(p.replace(/\n$/, ''))}</pre>`; return; }
    const fmt = (t) => esc(t).replace(/`([^`]+)`/g, '<code>$1</code>').replace(/\*\*([^*]+)\*\*/g, '<b>$1</b>').replace(/\*([^*\s][^*]*)\*/g, '<i>$1</i>');
    if (inline) { html += fmt(p); return; }
    html += p.split(/\n{2,}/).filter(x => x.trim()).map(par => {
      if (/^\s*[-•] /m.test(par)) return '<ul>' + par.split('\n').filter(l => l.trim()).map(l => `<li>${fmt(l.replace(/^\s*[-•] /, ''))}</li>`).join('') + '</ul>';
      return `<p>${fmt(par).replace(/\n/g, '<br>')}</p>`;
    }).join('');
  });
  return html;
}
