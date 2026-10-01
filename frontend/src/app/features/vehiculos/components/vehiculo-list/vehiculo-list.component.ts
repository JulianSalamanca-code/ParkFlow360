import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VehiculoService } from '../../services/vehiculo.service';
import { Vehiculo } from '../../models/vehiculo.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-vehiculo-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Vehículos</h1>
          <p>Registro y control de vehículos del parqueadero</p>
        </div>
        <a routerLink="/vehiculos/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Nuevo Vehículo
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Total Vehículos</div>
          <div class="value">{{ vehiculos.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Automóviles</div>
          <div class="value">{{ contarPorTipo('CARRO') }}</div>
        </div>
        <div class="metric metric-occupied">
          <div class="label">Motocicletas</div>
          <div class="value">{{ contarPorTipo('MOTO') }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Placa</th>
                <th>Tipo</th>
                <th>Color</th>
                <th>Modelo</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let vehiculo of vehiculos">
                <td><span class="plate">{{ vehiculo.placa }}</span></td>
                <td><span class="badge badge-neutral">{{ vehiculo.tipo }}</span></td>
                <td>{{ vehiculo.color }}</td>
                <td>{{ vehiculo.modelo }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/vehiculos/editar', vehiculo.id]" class="btn btn-sm btn-secondary">
                      <span class="material-symbols-outlined">edit</span> Editar
                    </a>
                    <button (click)="eliminar(vehiculo.id)" class="btn btn-sm btn-danger">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="vehiculos.length === 0">
                <td colspan="5" class="table-empty">No hay vehículos registrados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class VehiculoListComponent implements OnInit {
  vehiculos: Vehiculo[] = [];

  constructor(private vehiculoService: VehiculoService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.vehiculoService.listarTodos().subscribe({
      next: (data) => this.vehiculos = data,
      error: (err) => console.error('Error al cargar vehículos:', err)
    });
  }

  contarPorTipo(tipo: string): number {
    return this.vehiculos.filter(v => v.tipo?.toUpperCase() === tipo).length;
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este vehículo?')) {
      this.vehiculoService.eliminar(id).subscribe({
        next: () => {
          this.toast.success('Vehículo eliminado correctamente.');
          this.cargarVehiculos();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el vehículo.')
      });
    }
  }
}
