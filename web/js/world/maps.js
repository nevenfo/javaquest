// Cartes du monde de Javelune. Chaque carte : géométrie (MapBuilder), PNJ, objets, portails.
// Les quêtes se rattachent aux PNJ par leur champ `giver` et aux objets par `quest`.
import { MapBuilder, TILE, T, D } from './builder.js';

// Sprites de personnages (index dans Tiny Dungeon).
export const SPRITES = { wizard: 84, boy: 85, bald: 86, viking: 87, peasant: 88, knight: 96, knight2: 97, soldier: 98, girl: 99, elder: 100,
  slime: 108, cyclops: 109, imp: 110, hooded: 111, ranger: 112, bat: 120, ghost: 121, spider: 122, rat: 123, skeleton: 124 };

export const AVATARS = [
  { id: 'boy', label: 'Apprenti', sprite: 85 }, { id: 'girl', label: 'Apprentie', sprite: 99 },
  { id: 'ranger', label: 'Rôdeur·se', sprite: 112 }, { id: 'knight', label: 'Chevalier·ère', sprite: 97 }, { id: 'peasant', label: 'Voyageur·se', sprite: 88 },
];

function village() {
  const m = new MapBuilder(46, 32, { seed: 11 });
  m.forestBorder(2, [{ side: 'e', from: 14, to: 16 }, { side: 's', from: 21, to: 24 }, { side: 'w', from: 14, to: 16 }, { side: 'n', from: 34, to: 36 }]);
  m.plaza(17, 12, 12, 8);
  m.path(5, 15, 17, 15).path(5, 14, 17, 14).path(28, 15, 45, 15).path(28, 14, 45, 14);
  m.path(22, 19, 22, 31).path(23, 19, 23, 31);
  m.path(22, 7, 22, 12).path(11, 8, 11, 14).path(35, 9, 35, 14).path(11, 15, 11, 19).path(35, 0, 35, 9);
  m.house(9, 5, 'redWoodWide', { door: 'maison-ysolde' });
  m.house(20, 4, 'blueStoneWide', { door: 'sanctuaire' });
  m.house(34, 6, 'redChimney', { door: 'forge' });
  m.house(9, 20, 'blueStoneWide', { door: 'bibliotheque' });
  m.house(30, 22, 'blueWood', { door: 'maison-pip' });
  m.house(36, 22, 'redStone', { door: 'auberge' });
  // Décor
  m.setD(23, 15, TILE.well).setD(37, 9, D(74)).setD(38, 9, TILE.barrel).setD(33, 9, TILE.log);
  for (let x = 27; x <= 41; x++) m.setD(x, 27, TILE.fenceH);
  m.setD(29, 26, TILE.beehive).setD(31, 26, TILE.beehive).setD(41, 20, TILE.target);
  m.scatter(3, 3, 6, 10, 0.18, [TILE.tree, TILE.bush, TILE.pine]);
  m.scatter(38, 3, 6, 9, 0.16, [TILE.tree, TILE.pine]);
  m.scatter(3, 24, 8, 6, 0.16, [TILE.tree, TILE.bush]);
  m.scatter(14, 23, 6, 6, 0.10, [TILE.sprout, TILE.mushroom], false);
  m.scatter(26, 2, 6, 4, 0.12, [TILE.bush, TILE.tree]);
  return m.build();
}

function foret() {
  const m = new MapBuilder(48, 34, { seed: 23 });
  m.forestBorder(3, [{ side: 'w', from: 15, to: 17 }, { side: 'n', from: 22, to: 24 }], [TILE.autumn, TILE.autumnPine, TILE.tree]);
  m.path(0, 16, 12, 16).path(0, 15, 12, 15).path(12, 16, 12, 26).path(12, 26, 36, 26).path(24, 0, 24, 15).path(12, 15, 24, 15).path(24, 15, 40, 15).path(40, 15, 40, 8);
  m.plaza(34, 4, 10, 6);
  m.plaza(29, 22, 9, 7);
  m.scatter(4, 4, 18, 9, 0.22, [TILE.autumn, TILE.autumnPine, TILE.bush]);
  m.scatter(14, 18, 12, 6, 0.18, [TILE.autumn, TILE.tree, TILE.bush]);
  m.scatter(26, 17, 18, 4, 0.18, [TILE.autumnPine, TILE.autumn]);
  m.scatter(4, 28, 40, 3, 0.25, [TILE.autumn, TILE.autumnPine]);
  m.scatter(8, 4, 30, 25, 0.04, [TILE.mushroom, TILE.sprout], false);
  m.house(5, 18, 'redChimney', { door: 'cabane-sylve' });
  return m.build();
}

function mines() {
  const m = new MapBuilder(44, 34, { base: 'void', seed: 5 });
  m.room(18, 26, 9, 8);            // entrée
  m.room(4, 16, 14, 9);            // galerie ouest
  m.room(26, 15, 14, 10);          // galerie est
  m.room(15, 3, 15, 10);           // grande salle
  m.carve(22, 16, 2, 11).carve(17, 20, 5, 1).carve(24, 19, 3, 1).carve(22, 12, 2, 4).room(20, 13, 6, 3).carve(22, 12, 2, 5);
  m.carve(22, 33, 2, 1);
  m.setD(6, 18, TILE.barrelD).setD(7, 18, TILE.barrelD).setD(36, 17, TILE.anvil).setD(28, 22, TILE.table).setD(29, 22, TILE.stool);
  m.setD(16, 4, TILE.statue).setD(28, 4, TILE.statue);
  m.carve(22, 0, 2, 4);            // brèche au nord : escalier vers la crypte
  return m.build();
}

// Donjon optionnel de la région 1 : trois salles en enfilade (entrée, salle des runes, antre du golem) + un ossuaire latéral.
function crypte() {
  const m = new MapBuilder(40, 40, { base: 'void', seed: 71 });
  m.room(14, 29, 12, 9);           // entrée (gardien)
  m.room(14, 17, 12, 9);           // salle des runes
  m.room(2, 16, 10, 9);            // ossuaire (à l'ouest)
  m.room(10, 3, 20, 10);           // antre du golem
  m.carve(19, 25, 2, 5).carve(19, 12, 2, 6).carve(11, 20, 4, 2).carve(19, 37, 2, 3);
  m.setD(15, 30, TILE.statue).setD(24, 30, TILE.statue).setD(15, 35, TILE.grave).setD(24, 35, TILE.tomb);
  m.setD(16, 18, TILE.banner).setD(23, 18, TILE.banner).setD(17, 23, TILE.pedestal).setD(22, 23, TILE.pedestal);
  m.setD(3, 17, TILE.bookshelf).setD(4, 17, TILE.bookshelf).setD(8, 17, TILE.bookshelf).setD(3, 23, TILE.barrelD).setD(4, 23, TILE.barrelD).setD(9, 23, TILE.tomb);
  m.setD(11, 4, TILE.banner).setD(28, 4, TILE.banner).setD(12, 10, TILE.statue).setD(27, 10, TILE.statue).setD(16, 4, TILE.fountain).setD(23, 4, TILE.fountain);
  return m.build();
}

function artisans() {
  const m = new MapBuilder(46, 32, { seed: 31 });
  m.forestBorder(2, [{ side: 'w', from: 14, to: 16 }, { side: 's', from: 30, to: 33 }, { side: 'n', from: 22, to: 23 }, { side: 'e', from: 14, to: 15 }], [TILE.tree, TILE.pine]);
  m.plaza(14, 11, 18, 9);
  m.path(22, 0, 22, 10, 2);        // sentier nord : porte de la Fonderie des Gabarits
  m.path(0, 15, 14, 15).path(0, 14, 14, 14).path(31, 20, 31, 31).path(32, 20, 32, 31);
  m.house(6, 4, 'redChimney', { door: 'atelier-brom' });
  m.house(16, 4, 'redWoodWide', { door: 'atelier-records' });
  m.house(26, 4, 'blueStoneWide', { door: 'comptoir' });
  m.house(36, 6, 'redStone', { door: 'pompe' });
  m.house(8, 22, 'blueWood', { door: 'maison-points' });
  m.setD(20, 13, TILE.anvil).setD(21, 13, TILE.barrel).setD(25, 13, TILE.log).setD(28, 17, TILE.barrel);
  m.scatter(36, 14, 8, 14, 0.14, [TILE.tree, TILE.pine, TILE.bush]);
  m.scatter(3, 26, 24, 4, 0.15, [TILE.tree, TILE.bush]);
  return m.build();
}

// Donjon optionnel de la région 2 : entrée (fondeur), halle des fours, annexe des moules (à l'est), grande fonderie (gabarit).
function fonderie() {
  const m = new MapBuilder(40, 40, { base: 'void', seed: 83 });
  m.room(14, 29, 12, 9);           // entrée (fondeur)
  m.room(14, 17, 12, 9);           // halle des fours
  m.room(28, 16, 10, 9);           // annexe des moules (à l'est)
  m.room(10, 3, 20, 10);           // grande fonderie (gabarit)
  m.carve(19, 25, 2, 5).carve(19, 12, 2, 6).carve(25, 20, 4, 2).carve(19, 37, 2, 3);
  m.setD(15, 30, TILE.anvil).setD(24, 30, TILE.anvil).setD(15, 35, TILE.barrelD).setD(16, 35, TILE.barrelD).setD(24, 35, TILE.table).setD(23, 35, TILE.stool);
  m.setD(16, 18, TILE.banner).setD(23, 18, TILE.banner).setD(17, 23, TILE.pedestalLit).setD(22, 23, TILE.pedestalLit).setD(15, 22, TILE.anvil);
  m.setD(29, 17, TILE.bookshelf).setD(30, 17, TILE.bookshelf).setD(35, 17, TILE.bookshelf).setD(36, 17, TILE.bookshelf).setD(30, 22, TILE.table).setD(31, 22, TILE.table).setD(36, 22, TILE.barrelD).setD(36, 23, TILE.barrelD).setD(29, 23, TILE.chest);
  m.setD(11, 4, TILE.banner).setD(28, 4, TILE.banner).setD(12, 10, TILE.anvil).setD(27, 10, TILE.anvil).setD(16, 4, TILE.fountainLit).setD(23, 4, TILE.fountainLit);
  return m.build();
}

function tour() {
  const m = new MapBuilder(40, 36, { base: 'void', seed: 41 });
  m.room(14, 28, 12, 8); m.room(3, 17, 14, 10); m.room(23, 17, 14, 10); m.room(10, 3, 20, 12);
  m.carve(19, 27, 2, 2).carve(16, 21, 8, 1).carve(19, 14, 2, 14).carve(19, 35, 2, 1);
  m.carve(19, 0, 2, 4);            // brèche au nord : passage vers le Labyrinthe des Lignées
  m.setD(12, 4, TILE.banner).setD(27, 4, TILE.banner).setD(4, 18, TILE.bookshelf).setD(5, 18, TILE.bookshelf).setD(35, 18, TILE.statue);
  return m.build();
}

// Donjon optionnel de la région 3 : entrée (héraut), salle des miroirs, galerie des blasons (à l'est), cœur du labyrinthe (sphinx).
function labyrinthe() {
  const m = new MapBuilder(40, 40, { base: 'void', seed: 97 });
  m.room(13, 29, 14, 9);           // entrée (héraut)
  m.room(14, 17, 12, 9);           // salle des miroirs
  m.room(28, 16, 9, 9);            // galerie des blasons (à l'est)
  m.room(9, 3, 22, 10);            // cœur du labyrinthe (sphinx)
  m.carve(19, 25, 2, 5).carve(19, 12, 2, 6).carve(25, 20, 4, 2).carve(19, 37, 2, 3);
  m.setD(14, 30, TILE.statue).setD(25, 30, TILE.statue).setD(14, 35, TILE.bookshelf).setD(15, 35, TILE.bookshelf).setD(25, 35, TILE.table).setD(24, 35, TILE.stool);
  m.setD(15, 18, TILE.banner).setD(24, 18, TILE.banner).setD(16, 23, TILE.pedestalLit).setD(23, 23, TILE.pedestalLit).setD(19, 18, TILE.fountain);
  m.setD(29, 17, TILE.banner).setD(35, 17, TILE.banner).setD(30, 22, TILE.table).setD(31, 22, TILE.stool).setD(35, 22, TILE.chest).setD(35, 23, TILE.statue);
  m.setD(10, 4, TILE.banner).setD(29, 4, TILE.banner).setD(11, 10, TILE.statue).setD(28, 10, TILE.statue).setD(15, 4, TILE.fountainLit).setD(24, 4, TILE.fountainLit).setD(12, 4, TILE.bookshelf);
  return m.build();
}

function bibliotheque() {
  const m = new MapBuilder(40, 30, { base: 'void', seed: 53 });
  m.room(5, 4, 30, 24);
  for (let x = 8; x <= 31; x += 1) { if (x % 6 !== 1 && x % 6 !== 2) { m.setD(x, 8, TILE.bookshelf); m.setD(x, 14, TILE.bookshelf); m.setD(x, 20, TILE.bookshelf); } }
  m.setD(19, 24, TILE.table).setD(20, 24, TILE.table).setD(18, 24, TILE.stool).carve(19, 27, 2, 1);
  return m.build();
}

function citadelle() {
  const m = new MapBuilder(36, 28, { base: 'void', seed: 61 });
  m.room(4, 3, 28, 22);
  m.carve(17, 24, 2, 4);
  for (let y = 7; y <= 19; y += 4) { m.setD(9, y, TILE.table).setD(14, y, TILE.table).setD(21, y, TILE.table).setD(26, y, TILE.table); }
  m.setD(6, 4, TILE.banner).setD(29, 4, TILE.banner);
  return m.build();
}

// Portraits / noms des PNJ principaux (dialogues de quêtes dans content/quests).
export const NPCS = {
  ysolde: { name: 'Doyenne Ysolde', sprite: SPRITES.wizard, role: 'Gardienne du Sanctuaire de la Compilation' },
  pip: { name: 'Pip le marchand', sprite: SPRITES.viking, role: 'Marchand bavard' },
  garde: { name: 'Garde Aubin', sprite: SPRITES.knight, role: 'Garde de la porte sud' },
  meunier: { name: 'Maître Odon', sprite: SPRITES.bald, role: 'Meunier du village' },
  lina: { name: 'Lina', sprite: SPRITES.girl, role: 'Apprentie herboriste' },
  sylve: { name: 'Sylve la forestière', sprite: SPRITES.ranger, role: 'Gardienne du Bois des Embranchements' },
  gobelin: { name: 'Gobelin péagier', sprite: SPRITES.imp, role: 'Péage du pont' },
  mineur: { name: 'Grumm le mineur', sprite: SPRITES.peasant, role: 'Chef des mines' },
  spectre: { name: 'Spectre des Boucles', sprite: SPRITES.ghost, role: 'Hante les galeries' },
  brom: { name: 'Brom le forgeron', sprite: SPRITES.bald, role: 'Forgeron des Objets' },
  rosa: { name: 'Rosa la scribe', sprite: SPRITES.elder, role: 'Archiviste des records' },
  pompiste: { name: 'Gaspard', sprite: SPRITES.boy, role: 'Gardien de la pompe à élixir' },
  sentinelle: { name: 'Sentinelle des Contrats', sprite: SPRITES.knight2, role: 'Veille sur la Tour' },
  mage: { name: 'Mage Orlane', sprite: SPRITES.hooded, role: 'Maîtresse des hiérarchies' },
  lyra: { name: 'Lyra l\'archiviste', sprite: SPRITES.elder, role: 'Bibliothécaire des Collections' },
  gardien: { name: 'Gardien de la crypte', sprite: SPRITES.skeleton, role: "Veille à l'entrée de la crypte" },
  golem: { name: 'Golem des Boucles', sprite: SPRITES.cyclops, role: 'Gardien du sceau de la crypte' },
  fondeur: { name: 'Maître fondeur Orgel', sprite: SPRITES.viking, role: "Veille à l'entrée de la Fonderie" },
  heraut: { name: 'Héraut des Lignées', sprite: SPRITES.boy, role: "Veille à l'entrée du Labyrinthe" },
  sphinx: { name: 'Sphinx des Lignées', sprite: SPRITES.spider, role: 'Juge des hiérarchies' },
  gabarit: { name: 'Gabarit de Fer', sprite: SPRITES.soldier, role: 'Contremaître des moules' },
  examinateur: { name: 'Examinatrice Céleste', sprite: SPRITES.knight, role: 'Juge des épreuves' },
};

export const MAPS = {
  village: {
    name: 'Village de Javelune', build: village, spawn: { x: 22, y: 17 }, mapPos: { x: 50, y: 52 },
    npcs: [
      { id: 'ysolde', x: 13, y: 9 }, { id: 'pip', x: 25, y: 13 }, { id: 'garde', x: 24, y: 28 },
      { id: 'meunier', x: 33, y: 11, wander: true }, { id: 'lina', x: 15, y: 17, wander: true },
    ],
    objects: [
      { id: 'tableau', x: 20, y: 12, tile: TILE.sign, solid: true, action: 'board', label: 'Consulter le tableau des primes' },
      { id: 'grimoire-autel', x: 26, y: 8, tile: D(54), solid: true, action: 'grimoire', label: 'Lire le grimoire' },
    ],
    doorQuests: { sanctuaire: 'q01-porte' },
    portals: [
      { x: 45, y: 14, h: 3, to: 'foret', at: { x: 1, y: 16 }, requires: 'q04-lanterne', locked: 'La Doyenne veut d\'abord te voir maîtriser les variables (quête « La lanterne du meunier »).' },
      { x: 21, y: 31, w: 4, to: 'mines', at: { x: 22, y: 32 }, requires: 'f-foret', locked: 'Le garde Aubin : « Les mines sont dangereuses. Reviens après avoir traversé le Bois des Embranchements. »' },
      { x: 0, y: 14, h: 3, to: 'artisans', at: { x: 44, y: 15 }, requires: 'f-mines', locked: 'Le quartier des Artisans est fermé tant que les mines ne sont pas sûres.' },
      { x: 34, y: 0, w: 3, to: 'bibliotheque', at: { x: 19, y: 26 }, requires: 'f-tour', locked: 'La Grande Bibliothèque n\'ouvre qu\'aux maîtres des contrats (Tour).' },
    ],
  },
  foret: {
    name: 'Bois des Embranchements', build: foret, spawn: { x: 2, y: 16 }, mapPos: { x: 76, y: 46 },
    npcs: [{ id: 'sylve', x: 9, y: 22 }, { id: 'gobelin', x: 36, y: 25 }],
    objects: [
      { id: 'autel-foret', x: 39, y: 6, tile: D(56), solid: true, label: 'Examiner l\'autel' },
    ],
    portals: [{ x: 0, y: 15, h: 2, to: 'village', at: { x: 44, y: 15 } }],
  },
  mines: {
    name: 'Mines de Boucle-Profonde', build: mines, spawn: { x: 22, y: 31 }, mapPos: { x: 50, y: 84 },
    npcs: [{ id: 'mineur', x: 21, y: 29 }, { id: 'spectre', x: 22, y: 7 }],
    objects: [],
    portals: [{ x: 22, y: 33, w: 2, to: 'village', at: { x: 22, y: 30 } },
      { x: 22, y: 0, w: 2, to: 'crypte', at: { x: 19, y: 35 }, requires: 'f-mines', locked: "Un éboulis scellé bloque l'escalier du nord. Termine d'abord le coffre du spectre (dernière épreuve des mines) : la crypte ne s'ouvre qu'aux mineurs confirmés." }],
  },
  crypte: {
    name: 'Crypte de Boucle-Profonde', build: crypte, spawn: { x: 19, y: 36 }, mapPos: { x: 76, y: 82 },
    npcs: [{ id: 'gardien', x: 21, y: 33 }, { id: 'golem', x: 20, y: 6 }],
    objects: [],
    portals: [{ x: 19, y: 39, w: 2, to: 'mines', at: { x: 22, y: 5 } }],
  },
  artisans: {
    name: 'Quartier des Artisans', build: artisans, spawn: { x: 43, y: 15 }, mapPos: { x: 22, y: 50 },
    npcs: [{ id: 'brom', x: 8, y: 8 }, { id: 'rosa', x: 18, y: 8 }, { id: 'pompiste', x: 37, y: 10 }],
    objects: [],
    portals: [{ x: 45, y: 14, h: 2, to: 'village', at: { x: 1, y: 15 } },
      { x: 22, y: 0, w: 2, to: 'fonderie', at: { x: 19, y: 36 }, requires: 'f-forge', locked: "Le sentier du nord est barré par une herse : la Fonderie des Gabarits n'ouvre qu'aux forgerons d'objets confirmés. Termine d'abord l'atelier des armures de Brom." },
      { x: 30, y: 31, w: 4, to: 'tour', at: { x: 19, y: 34 }, requires: 'f-forge', locked: 'La Tour des Contrats n\'accepte que les forgerons d\'objets confirmés (quêtes de Brom).' }],
  },
  fonderie: {
    name: 'Fonderie des Gabarits', build: fonderie, spawn: { x: 19, y: 36 }, mapPos: { x: 22, y: 24 },
    npcs: [{ id: 'fondeur', x: 21, y: 33 }, { id: 'gabarit', x: 20, y: 6 }],
    objects: [],
    portals: [{ x: 19, y: 39, w: 2, to: 'artisans', at: { x: 22, y: 3 } }],
  },
  tour: {
    name: 'Tour des Contrats', build: tour, spawn: { x: 19, y: 33 }, mapPos: { x: 24, y: 80 },
    npcs: [{ id: 'sentinelle', x: 19, y: 31 }, { id: 'mage', x: 19, y: 7 }],
    objects: [],
    portals: [{ x: 19, y: 35, w: 2, to: 'artisans', at: { x: 31, y: 30 } },
      { x: 19, y: 0, w: 2, to: 'labyrinthe', at: { x: 19, y: 36 }, requires: 'f-tour', locked: "Une grille de fer barre le passage du nord : le Labyrinthe des Lignées n'ouvre qu'aux maîtres des contrats. Termine d'abord la salle des contrats de la Tour." }],
  },
  labyrinthe: {
    name: 'Labyrinthe des Lignées', build: labyrinthe, spawn: { x: 19, y: 36 }, mapPos: { x: 24, y: 66 },
    npcs: [{ id: 'heraut', x: 21, y: 33 }, { id: 'sphinx', x: 20, y: 6 }],
    objects: [],
    portals: [{ x: 19, y: 39, w: 2, to: 'tour', at: { x: 19, y: 4 } }],
  },
  bibliotheque: {
    name: 'Grande Bibliothèque', build: bibliotheque, spawn: { x: 19, y: 26 }, mapPos: { x: 76, y: 16 },
    npcs: [{ id: 'lyra', x: 19, y: 22 }],
    objects: [],
    portals: [{ x: 19, y: 27, w: 2, to: 'village', at: { x: 35, y: 2 } }],
  },
  citadelle: {
    name: 'Citadelle des Épreuves', build: citadelle, spawn: { x: 17, y: 23 }, mapPos: { x: 50, y: 14 },
    npcs: [{ id: 'examinateur', x: 17, y: 6 }],
    objects: [{ id: 'pupitre', x: 17, y: 11, tile: D(54), solid: true, action: 'exam', label: 'Pupitre des épreuves (mode examen)' }],
    portals: [{ x: 17, y: 27, w: 2, to: 'village', at: { x: 22, y: 10 } }],
  },
};
