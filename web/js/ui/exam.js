// Citadelle des Épreuves : accès au mode examen (implémentation complète : phase G1).
export class ExamHall {
  constructor(ctx) { this.ctx = ctx; }
  enter() { this.ctx.G.scene.loadMap('citadelle', { x: 17, y: 23 }); }
}
