// Boîte de dialogue des PNJ : pages successives, choix, portrait pixel.
import { md } from './text.js';
import { play } from '../sound.js';

const $ = (id) => document.getElementById(id);

export class Dialog {
  constructor() {
    this.el = $('dialog');
    this.queue = null;
    addEventListener('keydown', (e) => {
      if (this.el.hidden) return;
      if (['Space', 'Enter', 'KeyE', 'NumpadEnter'].includes(e.code) && !this.choices) { e.preventDefault(); this.next(); }
      if (e.code === 'Escape') { e.preventDefault(); this.close(); }
      if (this.choices && /^Digit[1-9]$/.test(e.code)) { const b = $('dialog-choices').children[+e.code.slice(5) - 1]; b?.click(); }
    });
    this.el.addEventListener('click', (e) => { if (!e.target.closest('button') && !this.choices) this.next(); });
  }

  get open() { return !this.el.hidden; }

  /** pages : [{ text, choices?: [{ label, action }] }] ; speaker : { name, sprite } */
  show(speaker, pages) {
    return new Promise((resolve) => {
      this.speaker = speaker; this.pages = pages.map(p => typeof p === 'string' ? { text: p } : p); this.i = 0; this.resolve = resolve;
      $('dialog-name').textContent = speaker.name;
      $('dialog-portrait').style.backgroundImage = speaker.portrait ? `url(${speaker.portrait})` : 'none';
      this.el.hidden = false;
      this.render();
      play('select');
    });
  }

  render() {
    const p = this.pages[this.i];
    $('dialog-text').innerHTML = md(p.text);
    const box = $('dialog-choices');
    box.innerHTML = '';
    this.choices = p.choices && p.choices.length ? p.choices : null;
    $('dialog-next').hidden = !!this.choices;
    if (this.choices) this.choices.forEach((c, k) => {
      const b = document.createElement('button');
      b.innerHTML = `<b>${k + 1}.</b> ${md(c.label, true)}`;
      b.onclick = () => { play('click'); this.close(c.value ?? k); c.action?.(); };
      box.appendChild(b);
      if (k === 0) setTimeout(() => b.focus(), 20);
    });
  }

  next() {
    if (this.i < this.pages.length - 1) { this.i++; play('tick'); this.render(); }
    else this.close(null);
  }

  close(value = null) {
    if (this.el.hidden) return;
    this.el.hidden = true;
    const r = this.resolve; this.resolve = null;
    r?.(value);
  }
}
