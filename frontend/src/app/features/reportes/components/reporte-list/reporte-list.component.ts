import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../../services/reporte.service';
import { Reporte } from '../../models/reporte.model';

@Component({
  selector: 'app-reporte-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Reportes</h1>
          <p>Resumen operativo y financiero del parqueadero</p>
        </div>
      </div>

      <div class="section-title">Resumen General</div>
      <div class="metrics">
        <div class="metric" *ngFor="let r of reporteGeneral"
             [ngClass]="r.tipo === 'PAGOS' ? 'metric-available' : ''">
          <div class="label">{{ r.tipo }}</div>
          <div class="value">
            <ng-container *ngIf="r.tipo === 'PAGOS'; else cantidad">
              {{ r.total | currency:'COP':'symbol-narrow':'1.0-0' }}
            </ng-container>
            <ng-template #cantidad>{{ r.cantidad }}</ng-template>
          </div>
          <div class="text-muted" style="font-size:12px;margin-top:4px">{{ r.descripcion }}</div>
        </div>
      </div>

      <div class="section-title">Ocupación por Estado</div>
      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Estado</th>
                <th>Cantidad</th>
                <th>Descripción</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of espaciosPorEstado">
                <td>
                  <span class="badge" [ngClass]="estadoBadge(r.tipo)">
                    {{ r.tipo | slice:8 }}
                  </span>
                </td>
                <td class="num">{{ r.cantidad }}</td>
                <td>{{ r.descripcion }}</td>
              </tr>
              <tr *ngIf="espaciosPorEstado.length === 0">
                <td colspan="3" class="table-empty">Sin datos de ocupación</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class ReporteListComponent implements OnInit {
  reporteGeneral: Reporte[] = [];
  espaciosPorEstado: Reporte[] = [];

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    this.cargarReportes();
  }

  cargarReportes(): void {
    this.reporteService.reporteGeneral().subscribe({
      next: (data) => this.reporteGeneral = data,
      error: (err) => console.error('Error al cargar reporte general:', err)
    });

    this.reporteService.espaciosPorEstado().subscribe({
      next: (data) => this.espaciosPorEstado = data,
      error: (err) => console.error('Error al cargar espacios por estado:', err)
    });
  }

  estadoBadge(tipo: string): string {
    switch (tipo?.toUpperCase()) {
      case 'ESPACIO_LIBRE': return 'badge-available';
      case 'ESPACIO_OCUPADO': return 'badge-occupied';
      case 'ESPACIO_RESERVADO': return 'badge-warning';
      default: return 'badge-neutral';
    }
  }
}
