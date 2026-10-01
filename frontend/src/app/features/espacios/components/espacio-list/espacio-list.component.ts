import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EspacioService } from '../../services/espacio.service';
import { Espacio } from '../../models/espacio.model';

@Component({
  selector: 'app-espacio-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <div>
            <h1>Espacios</h1>
            <p class="page-subtitle">Disponibilidad y estado de los espacios de parqueo</p>
          </div>
        </div>
        <a routerLink="/espacios/nuevo" class="btn btn-primary">+ Nuevo Espacio</a>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">Total</div>
          <div class="stat-value">{{ espacios.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Libres</div>
          <div class="stat-value">{{ contarPorEstado('LIBRE') }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Ocupados</div>
          <div class="stat-value">{{ contarPorEstado('OCUPADO') }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrapper">
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
                <td><strong>{{ espacio.numero }}</strong></td>
                <td>{{ espacio.tipo }}</td>
                <td>
                  <span class="badge" [ngClass]="getEstadoClass(espacio.estado)">
                    {{ espacio.estado }}
                  </span>
                </td>
                <td>{{ espacio.piso }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/espacios/editar', espacio.id]" class="btn btn-sm btn-warning">Editar</a>
                    <button (click)="eliminar(espacio.id)" class="btn btn-sm btn-danger">Eliminar</button>
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

  constructor(private espacioService: EspacioService) {}

  ngOnInit(): void {
    this.cargarEspacios();
  }

  cargarEspacios(): void {
    this.espacioService.listarTodos().subscribe({
      next: (data) => this.espacios = data,
      error: (err) => console.error('Error al cargar espacios:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este espacio?')) {
      this.espacioService.eliminar(id).subscribe({
        next: () => this.cargarEspacios(),
        error: (err) => console.error('Error al eliminar:', err)
      });
    }
  }

  contarPorEstado(estado: string): number {
    return this.espacios.filter(e => e.estado?.toUpperCase() === estado).length;
  }

  getEstadoClass(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'LIBRE': return 'badge-libre';
      case 'OCUPADO': return 'badge-ocupado';
      case 'RESERVADO': return 'badge-reservado';
      case 'MANTENIMIENTO': return 'badge-mantenimiento';
      default: return '';
    }
  }
}
