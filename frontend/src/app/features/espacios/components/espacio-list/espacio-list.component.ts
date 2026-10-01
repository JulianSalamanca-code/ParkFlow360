import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EspacioService } from '../../services/espacio.service';
import { Espacio } from '../../models/espacio.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { IngresoService } from '../../../ingresos/services/ingreso.service';

@Component({
  selector: 'app-espacio-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Espacios</h1>
          <p>Disponibilidad y estado de los espacios de parqueo</p>
        </div>
        <a routerLink="/espacios/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Nuevo Espacio
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Capacidad Total</div>
          <div class="value">{{ espacios.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Disponibles</div>
          <div class="value">{{ contarPorEstado('LIBRE') }}</div>
        </div>
        <div class="metric metric-occupied">
          <div class="label">Ocupados</div>
          <div class="value">{{ contarPorEstado('OCUPADO') }}</div>
        </div>
        <div class="metric metric-warning">
          <div class="label">Reservados</div>
          <div class="value">{{ contarPorEstado('RESERVADO') }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Número</th>
                <th>Tipo</th>
                <th>Estado</th>
                <th>Piso</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let espacio of espacios">
                <td><span class="plate">{{ espacio.numero }}</span></td>
                <td><span class="badge badge-neutral">{{ espacio.tipo }}</span></td>
                <td>
                  <span class="badge" [ngClass]="getEstadoClass(espacio.estado)">
                    {{ espacio.estado }}
                  </span>
                </td>
                <td class="num">{{ espacio.piso }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/espacios/editar', espacio.id]" class="btn btn-sm btn-secondary">
                      <span class="material-symbols-outlined">edit</span> Editar
                    </a>
                    <button *ngIf="espacio.estado === 'RESERVADO' || espacio.estado === 'OCUPADO'"
                            (click)="liberar(espacio)" class="btn btn-sm btn-warning"
                            title="Liberar reserva / parqueo">
                      <span class="material-symbols-outlined">event_busy</span> Liberar
                    </button>
                    <button (click)="eliminar(espacio.id)" class="btn btn-sm btn-danger">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="espacios.length === 0">
                <td colspan="5" class="table-empty">No hay espacios registrados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class EspacioListComponent implements OnInit {
  espacios: Espacio[] = [];

  constructor(
    private espacioService: EspacioService,
    private ingresoService: IngresoService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarEspacios();
  }

  cargarEspacios(): void {
    this.espacioService.listarTodos().subscribe({
      next: (data) => this.espacios = data,
      error: (err) => console.error('Error al cargar espacios:', err)
    });
  }

  contarPorEstado(estado: string): number {
    return this.espacios.filter(e => e.estado?.toUpperCase() === estado).length;
  }

  getEstadoClass(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'LIBRE': return 'badge-available';
      case 'OCUPADO': return 'badge-occupied';
      case 'RESERVADO': return 'badge-warning';
      default: return 'badge-neutral';
    }
  }

  liberar(espacio: Espacio): void {
    if (confirm(`¿Liberar el espacio ${espacio.numero}? La reserva o parqueo activo se cancelará.`)) {
      this.ingresoService.liberarPorEspacio(espacio.id).subscribe({
        next: () => {
          this.toast.success(`Espacio ${espacio.numero} liberado correctamente.`);
          this.cargarEspacios();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo liberar el espacio.')
      });
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este espacio?')) {
      this.espacioService.eliminar(id).subscribe({
        next: () => {
          this.toast.success('Espacio eliminado correctamente.');
          this.cargarEspacios();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el espacio.')
      });
    }
  }
}
