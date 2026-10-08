import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PlanoService } from '../../services/plano.service';
import { Plano } from '../../models/plano.model';
import { Espacio } from '../../../espacios/models/espacio.model';
import { ToastService } from '../../../../shared/services/toast.service';

interface Celda {
  fila: number;
  columna: number;
  espacio?: Espacio;
}

@Component({
  selector: 'app-plano-mapa',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ plano?.nombre || 'Mapa de ocupación' }}</h1>
          <p>Estado de los espacios en tiempo real · {{ plano?.descripcion }}</p>
        </div>
        <a routerLink="/planos" class="btn btn-secondary">
          <span class="material-symbols-outlined">arrow_back</span> Volver
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Total</div>
          <div class="value">{{ espacios.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Libres</div>
          <div class="value">{{ contar('LIBRE') }}</div>
        </div>
        <div class="metric metric-occupied">
          <div class="label">Ocupados</div>
          <div class="value">{{ contar('OCUPADO') }}</div>
        </div>
        <div class="metric metric-warning">
          <div class="label">Reservados</div>
          <div class="value">{{ contar('RESERVADO') }}</div>
        </div>
      </div>

      <div class="mapa-wrap" [style.background-image]="plano?.imagenUrl ? 'url(' + plano?.imagenUrl + ')' : null">
        <div class="grid" [style.grid-template-columns]="'repeat(' + columnas + ', minmax(0, 1fr))'">
          <div class="celda" *ngFor="let c of celdas"
               [ngClass]="c.espacio ? estadoClase(c.espacio.estado) : 'vacio'"
               [title]="c.espacio ? (c.espacio.numero + ' · ' + c.espacio.estado) : 'Sin espacio'">
            <span class="celda-num" *ngIf="c.espacio">{{ c.espacio.numero }}</span>
            <span class="celda-estado" *ngIf="c.espacio">{{ c.espacio.estado }}</span>
          </div>
        </div>
      </div>

      <div class="leyenda">
        <span><i class="dot libre"></i> Libre</span>
        <span><i class="dot ocupado"></i> Ocupado</span>
        <span><i class="dot reservado"></i> Reservado</span>
        <span><i class="dot mantenimiento"></i> Mantenimiento</span>
      </div>
    </div>
  `,
  styles: [`
    .mapa-wrap {
      background-color: #0f172a;
      background-size: contain;
      background-repeat: no-repeat;
      background-position: center;
      border: 1px solid #1e293b;
      border-radius: var(--pf-radius);
      padding: 24px;
      min-height: 320px;
    }
    .grid { display: grid; gap: 8px; }
    .celda {
      aspect-ratio: 3 / 2;
      border-radius: 6px;
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 2px; padding: 4px; text-align: center; overflow: hidden;
      /* Fondo semitransparente para que se vea el plano dibujado debajo */
      border: 1px solid rgba(226, 232, 240, 0.35);
      color: #f8fafc;
      text-shadow: 0 1px 3px rgba(2, 6, 23, 0.9);
    }
    .celda-num { font-family: var(--pf-mono); font-weight: 700; font-size: 13px; }
    .celda-estado { font-size: 9px; text-transform: uppercase; letter-spacing: 0.04em; opacity: 0.9; }
    .vacio { background: rgba(15, 23, 42, 0.35); border: 1px dashed rgba(148, 163, 184, 0.5); }
    .libre { background: rgba(16, 185, 129, 0.34); border-color: rgba(52, 211, 153, 0.8); }
    .ocupado { background: rgba(239, 68, 68, 0.36); border-color: rgba(248, 113, 113, 0.85); }
    .reservado { background: rgba(245, 158, 11, 0.36); border-color: rgba(251, 191, 36, 0.85); }
    .mantenimiento { background: rgba(100, 116, 139, 0.4); border-color: rgba(148, 163, 184, 0.6); }
    .leyenda { display: flex; flex-wrap: wrap; gap: 18px; margin-top: 16px; color: var(--pf-ink-soft); font-size: 13px; }
    .leyenda span { display: inline-flex; align-items: center; gap: 6px; }
    .dot { width: 12px; height: 12px; border-radius: 3px; display: inline-block; }
    .dot.libre { background: var(--pf-available); }
    .dot.ocupado { background: var(--pf-occupied); }
    .dot.reservado { background: var(--pf-warning); }
    .dot.mantenimiento { background: var(--pf-muted); }
  `]
})
export class PlanoMapaComponent implements OnInit {
  plano?: Plano;
  espacios: Espacio[] = [];
  celdas: Celda[] = [];

  constructor(
    private planoService: PlanoService,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.planoService.obtenerPorId(id).subscribe({
      next: (p) => {
        this.plano = p;
        this.cargarEspacios(id);
      },
      error: () => this.toast.error('No se pudo cargar el plano.')
    });
  }

  cargarEspacios(id: number): void {
    this.planoService.listarEspacios(id).subscribe({
      next: (data) => {
        this.espacios = data;
        this.construirCeldas();
      },
      error: () => this.toast.error('No se pudieron cargar los espacios del plano.')
    });
  }

  get columnas(): number {
    return Math.max(1, this.plano?.columnas || 1);
  }

  private construirCeldas(): void {
    const filas = Math.max(1, this.plano?.filas || 1);
    const columnas = this.columnas;
    const celdas: Celda[] = [];

    for (let f = 1; f <= filas; f++) {
      for (let c = 1; c <= columnas; c++) {
        const espacio = this.espacios.find(e => e.fila === f && e.columna === c);
        celdas.push({ fila: f, columna: c, espacio });
      }
    }
    this.celdas = celdas;
  }

  contar(estado: string): number {
    return this.espacios.filter(e => e.estado?.toUpperCase() === estado).length;
  }

  estadoClase(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'LIBRE': return 'libre';
      case 'OCUPADO': return 'ocupado';
      case 'RESERVADO': return 'reservado';
      default: return 'mantenimiento';
    }
  }
}
