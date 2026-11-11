// Construction déclarative des cartes à partir des tuiles Kenney (16×16).
// GID 1..132 : Tiny Town (« t ») ; GID 133..264 : Tiny Dungeon (« d »). 0 = vide.
export const T = (i) => i + 1;          // tuile Tiny Town
export const D = (i) => i + 133;        // tuile Tiny Dungeon

export const TILE = {
  grass: [T(0), T(0), T(0), T(0), T(1), T(0), T(0), T(2)],
  stonePath: T(43),
  dirt: { tl: T(12), t: T(13), tr: T(14), l: T(24), c: T(25), r: T(26), bl: T(36), b: T(37), br: T(38) },
  tree: T(16), pine: T(28), autumn: T(15), autumnPine: T(27), bush: T(5), sprout: T(17), mushroom: T(29),
  fenceH: T(81), fenceL: T(80), fenceR: T(82), fenceV: T(59), sign: T(83), well: T(104), beehive: T(94), target: T(95),
  barrel: T(107), log: T(106), bucket: T(131),
  // Donjon
  floor: [D(48), D(48), D(48), D(49), D(48), D(51)], void: D(0), wall: D(40), wallB: D(57), wallTop: D(37), rubble: D(24), rubble2: D(12),
  bookshelf: D(63), table: D(72), stool: D(73), anvil: D(74), barrelD: D(82), chest: D(89), chestOpen: D(90),
  statue: D(19), statueLit: D(20), fountain: D(31), fountainLit: D(32), pedestal: D(7), pedestalLit: D(8),
  spikes: D(41), grave: D(64), tomb: D(65), bars: D(67), doorWood: D(45), doorDouble: [D(46), D(47)], banner: D(29),
  potionBlue: D(116), potionRed: D(115), potionGreen: D(114), key: T(117), coin: T(93),
};

/** Maisons 3 ou 4 tuiles de large : [ligne toit haut, ligne toit bas, ligne murs]. Index de la porte dans la ligne murs. */
export const HOUSES = {
  blueWood: { rows: [[48, 49, 50], [60, 63, 62], [72, 85, 75]], door: 1, open: 74 },
  redStone: { rows: [[52, 53, 54], [64, 67, 66], [76, 89, 79]], door: 1, open: 78 },
  redWoodWide: { rows: [[52, 53, 53, 54], [64, 65, 67, 66], [84, 73, 86, 75]], door: 2, open: 74 },
  blueStoneWide: { rows: [[48, 49, 49, 50], [60, 61, 63, 62], [88, 77, 90, 79]], door: 2, open: 78 },
  redChimney: { rows: [[52, 55, 54], [64, 67, 66], [72, 87, 75]], door: 1, open: 74 },
};

export class MapBuilder {
  constructor(w, h, { base = 'grass', seed = 7 } = {}) {
    this.w = w; this.h = h; this.seed = seed;
    this.ground = new Array(w * h).fill(0);
    this.deco = new Array(w * h).fill(0);
    this.over = new Array(w * h).fill(0); // au-dessus du joueur (toits)
    this.solid = new Uint8Array(w * h);
    this.doors = [];
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) this.ground[y * w + x] = this.baseTile(base, x, y);
  }

  rnd(x, y) { // pseudo-aléatoire déterministe par case
    let n = (x * 374761393 + y * 668265263 + this.seed * 982451653) | 0;
    n = (n ^ (n >>> 13)) * 1274126177;
    return ((n ^ (n >>> 16)) >>> 0) / 4294967296;
  }

  baseTile(base, x, y) {
    if (base === 'grass') return TILE.grass[Math.floor(this.rnd(x, y) * TILE.grass.length)];
    if (base === 'floor') return TILE.floor[Math.floor(this.rnd(x, y) * TILE.floor.length)];
    if (base === 'void') return TILE.void;
    return base;
  }

  in(x, y) { return x >= 0 && y >= 0 && x < this.w && y < this.h; }
  setG(x, y, g) { if (this.in(x, y)) this.ground[y * this.w + x] = g; return this; }
  setD(x, y, g, solid = true) { if (this.in(x, y)) { this.deco[y * this.w + x] = g; this.solid[y * this.w + x] = solid ? 1 : 0; } return this; }
  setO(x, y, g) { if (this.in(x, y)) this.over[y * this.w + x] = g; return this; }
  block(x, y, v = 1) { if (this.in(x, y)) this.solid[y * this.w + x] = v; return this; }
  free(x, y) { if (this.in(x, y)) { this.deco[y * this.w + x] = 0; this.solid[y * this.w + x] = 0; } return this; }

  fillG(x, y, w, h, g) { for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) this.setG(x + i, y + j, typeof g === 'function' ? g(x + i, y + j) : g); return this; }

  /** Chemin pavé en L entre deux points. */
  path(x1, y1, x2, y2, width = 1) {
    const sx = Math.sign(x2 - x1), sy = Math.sign(y2 - y1);
    for (let x = x1; x !== x2 + sx || (sx === 0 && x === x1); x += sx || 1) {
      for (let k = 0; k < width; k++) this.setG(x, y1 + k, TILE.stonePath);
      if (sx === 0) break;
    }
    for (let y = y1; y !== y2 + sy || (sy === 0 && y === y1); y += sy || 1) {
      for (let k = 0; k < width; k++) this.setG(x2 + k, y, TILE.stonePath);
      if (sy === 0) break;
    }
    return this;
  }

  /** Place de terre battue avec bordures herbeuses. */
  plaza(x, y, w, h) {
    const d = TILE.dirt;
    for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
      const top = j === 0, bot = j === h - 1, left = i === 0, right = i === w - 1;
      const t = top ? (left ? d.tl : right ? d.tr : d.t) : bot ? (left ? d.bl : right ? d.br : d.b) : left ? d.l : right ? d.r : d.c;
      this.setG(x + i, y + j, t);
    }
    return this;
  }

  /** Bordure d'arbres (épaisseur e) avec ouvertures [{side,from,to}]. */
  forestBorder(e = 2, openings = [], kinds = [TILE.tree, TILE.pine]) {
    const isOpen = (x, y) => openings.some(o =>
      (o.side === 'n' && y < e && x >= o.from && x <= o.to) || (o.side === 's' && y >= this.h - e && x >= o.from && x <= o.to) ||
      (o.side === 'w' && x < e && y >= o.from && y <= o.to) || (o.side === 'e' && x >= this.w - e && y >= o.from && y <= o.to));
    for (let y = 0; y < this.h; y++) for (let x = 0; x < this.w; x++) {
      if ((x < e || y < e || x >= this.w - e || y >= this.h - e) && !isOpen(x, y))
        this.setD(x, y, kinds[Math.floor(this.rnd(x + 3, y + 5) * kinds.length)]);
    }
    return this;
  }

  /** Arbres épars dans une zone, en évitant les cases déjà occupées et les chemins. */
  scatter(x, y, w, h, density, kinds, solid = true) {
    for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
      const X = x + i, Y = y + j, k = Y * this.w + X;
      if (!this.in(X, Y) || this.deco[k] || this.ground[k] === TILE.stonePath || Object.values(TILE.dirt).includes(this.ground[k])) continue;
      if (this.rnd(X * 7, Y * 3) < density) this.setD(X, Y, kinds[Math.floor(this.rnd(X, Y * 11) * kinds.length)], solid);
    }
    return this;
  }

  /** Maison : toit (2 rangées) au-dessus du joueur, murs solides, porte (id) éventuellement franchissable. */
  house(x, y, kind, { door = null } = {}) {
    const H = HOUSES[kind];
    H.rows.forEach((row, j) => row.forEach((t, i) => {
      if (j < 2) { this.setD(x + i, y + j, T(t)); }
      else this.setD(x + i, y + j, T(t));
    }));
    if (door) this.doors.push({ id: door, x: x + H.door, y: y + 2, closed: T(H.rows[2][H.door]), open: T(H.open) });
    return this;
  }

  /** Salle de donjon : murs autour (tuile mur), sol intérieur. */
  room(x, y, w, h, { wall = TILE.wall } = {}) {
    for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
      const X = x + i, Y = y + j;
      const edge = i === 0 || j === 0 || i === w - 1 || j === h - 1;
      if (edge) { this.setG(X, Y, this.baseTile('floor', X, Y)); this.setD(X, Y, wall); }
      else { this.setG(X, Y, this.baseTile('floor', X, Y)); this.free(X, Y); }
    }
    return this;
  }

  /** Ouvre un passage (sol) dans des murs. */
  carve(x, y, w = 1, h = 1) {
    for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) { this.setG(x + i, y + j, this.baseTile('floor', x + i, y + j)); this.free(x + i, y + j); }
    return this;
  }

  build() {
    return { w: this.w, h: this.h, ground: this.ground, deco: this.deco, over: this.over, solid: this.solid, doors: this.doors };
  }
}
