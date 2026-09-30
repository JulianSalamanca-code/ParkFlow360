import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../../services/reporte.service';
import { Reporte } from '../../models/reporte.model';

@Component({
  selector: 'app-reporte-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="reporte-list">
      <h2>Reportes</h2>

      <div class="section">
        <h3>Reporte General</h3>
        <table class="table">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Cantidad</th>
              <th>Total</th>
              <th>Descripción</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let reporte of reporteGeneral">
              <td>{{ reporte.tipo }}</td>
              <td>{{ reporte.cantidad }}</td>
              <td>{{ reporte.total | currency }}</td>
              <td>{{ reporte.descripcion }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="section">
        <h3>Espacios por Estado</h3>
        <table class="table">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Cantidad</th>
              <th>Descripción</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let reporte of espaciosPorEstado">
              <td>{{ reporte.tipo }}</td>
              <td>{{ reporte.cantidad }}</td>
              <td>{{ reporte.descripcion }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .reporte-list { padding: 20px; }
    .section { margin-bottom: 30px; }
    .section h3 { margin-bottom: 15px; color: #333; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    .table th { background-color: #f5f5f5; font-weight: bold; }
  `]
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
