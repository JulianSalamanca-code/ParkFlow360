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
          <div>
            <h1>Reportes</h1>
            <p class="page-subtitle">Resumen operativo y financiero del parqueadero</p>
          </div>
        </div>
      </div>

      <div class="section-title">Resumen General</div>
      <div class="stats-grid">
        <div class="stat-card" *ngFor="let r of reporteGeneral">
          <div class="stat-label">{{ r.descripcion }}</div>
          <div class="stat-value">
            <ng-container *ngIf="r.tipo === 'PAGOS'; else cantidad">{{ r.total | currency:'COP':'symbol-narrow':'1.0-0' }}</ng-container>
            <ng-template #cantidad>{{ r.cantidad }}</ng-template>
          </div>
        </div>
      </div>

      <div class="section-title">Espacios por Estado</div>
      <div class="card">
        <div class="table-wrapper">
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
                <td><strong>{{ r.tipo | slice:8 }}</strong></td>
                <td>{{ r.cantidad }}</td>
                <td>{{ r.descripcion }}</td>
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
}
