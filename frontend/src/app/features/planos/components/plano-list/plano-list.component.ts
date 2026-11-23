import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PlanoService } from '../../services/plano.service';
import { Plano } from '../../models/plano.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-plano-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Plano y Ocupación</h1>
          <p>Carga el plano del parqueadero y genera los espacios en bloque</p>
        </div>
        <a routerLink="/planos/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Nuevo Plano
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Planos</div>
          <div class="value">{{ planos.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Espacios totales</div>
          <div class="value">{{ totalEspacios }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Piso</th>
                <th>Cuadrícula</th>
                <th>Espacios</th>
                <th>Creado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let p of planos">
                <td><strong>{{ p.nombre }}</strong><div class="text-muted" style="font-size:12px">{{ p.descripcion }}</div></td>
                <td class="num">{{ p.piso }}</td>
                <td class="num">{{ p.filas }} × {{ p.columnas }}</td>
                <td class="num">{{ p.totalEspacios }}</td>
                <td class="mono">{{ p.fechaCreacion | date:'short' }}</td>
                <td>
                  <div class="table-actions">
                    <a class="btn btn-sm btn-secondary" [routerLink]="['/planos', p.id, 'mapa']" title="Ver mapa">
                      <span class="material-symbols-outlined">map</span>
                    </a>
                    <a class="btn btn-sm btn-secondary" [routerLink]="['/planos/editar', p.id]" title="Editar">
                      <span class="material-symbols-outlined">edit</span>
                    </a>
                    <button class="btn btn-sm btn-danger" (click)="eliminar(p)" title="Eliminar">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="planos.length === 0">
                <td colspan="6" class="table-empty">Aún no hay planos. Crea uno para generar los espacios.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .table-actions { display: flex; gap: 6px; }
  `]
})
export class PlanoListComponent implements OnInit {
  planos: Plano[] = [];

  constructor(private planoService: PlanoService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargar();
  }

  get totalEspacios(): number {
    return this.planos.reduce((acc, p) => acc + (p.totalEspacios || 0), 0);
  }

  cargar(): void {
    this.planoService.listarTodos().subscribe({
      next: (data) => this.planos = data,
      error: () => this.toast.error('No se pudieron cargar los planos.')
    });
  }

  eliminar(plano: Plano): void {
    if (!confirm(`¿Eliminar el plano "${plano.nombre}" y sus ${plano.totalEspacios} espacios?`)) {
      return;
    }
    this.planoService.eliminar(plano.id).subscribe({
      next: () => {
        this.toast.success('Plano eliminado.');
        this.cargar();
      },
      error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el plano.')
    });
  }
}
