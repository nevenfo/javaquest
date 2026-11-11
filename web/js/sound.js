// Effets sonores (Kenney, CC0). Volume réglable, désactivable.
const files = {
  step: ['footstep00', 'footstep01', 'footstep02', 'footstep03'],
  door: ['doorOpen_1'], doorClose: ['doorClose_1'], book: ['bookOpen'], bookClose: ['bookClose'], page: ['bookFlip1'],
  coins: ['handleCoins'], latch: ['metalLatch'], creak: ['creak1'],
  ok: ['confirmation_001'], error: ['error_004'], select: ['select_001'], click: ['click_002'],
  open: ['open_001'], close: ['close_001'], magic: ['maximize_003'], sparkle: ['glass_002'], question: ['question_001'],
  drop: ['drop_002'], tick: ['tick_001'],
};
const cache = new Map();
let volume = 0.6;

export function setVolume(v) { volume = Math.max(0, Math.min(1, v)); }
export function getVolume() { return volume; }

export function play(name, rate = 1) {
  if (volume <= 0 || !files[name]) return;
  const list = files[name];
  const f = list[Math.floor(Math.random() * list.length)];
  let base = cache.get(f);
  if (!base) { base = new Audio(`assets/sfx/${f}.ogg`); cache.set(f, base); }
  const a = base.cloneNode();
  a.volume = volume * (name === 'step' ? 0.35 : 1);
  a.playbackRate = rate;
  a.play().catch(() => { /* lecture bloquée avant interaction */ });
}
