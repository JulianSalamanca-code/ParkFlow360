import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VehiculoService } from '../../../../vehiculos/services/vehiculo.service';
import { Vehiculo } from '../../../../vehiculos/models/vehiculo.model';
import { ToastService } from '../../../../../shared/services/toast.service';

@Component({
  selector: 'app-mis-vehiculos-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Mis Vehículos</h1>
          <p>Vehículos registrados a tu nombre</p>
        </div>
        <a routerLink="/mis-vehiculos/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Agregar Vehículo
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Mis Vehículos</div>
          <div class="value">{{ vehiculos.length }}</div>
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
              <tr *ngFor="let v of vehiculos">
                <td><span class="plate">{{ v.placa }}</span></td>
                <td><span class="badge badge-neutral">{{ v.tipo }}</span></td>
                <td>{{ v.color }}</td>
                <td>{{ v.modelo }}</td>
                <td>
                  <div class="table-actions">
                    <a class="btn btn-sm btn-secondary" [routerLink]="['/mis-vehiculos/editar', v.id]" title="Editar">
                      <span class="material-symbols-outlined">edit</span>
                    </a>
                    <button class="btn btn-sm btn-danger" (click)="eliminar(v)" title="Eliminar">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="vehiculos.length === 0">
                <td colspan="5" class="table-empty">Aún no tienes vehículos registrados</td>
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
export class MisVehiculosListComponent implements OnInit {
  vehiculos: Vehiculo[] = [];

  constructor(private vehiculoService: VehiculoService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.vehiculoService.misVehiculos().subscribe({
      next: (data) => this.vehiculos = data,
      error: (err) => console.error('Error al cargar mis vehículos:', err)
    });
  }

  eliminar(v: Vehiculo): void {
    if (!confirm(`¿Eliminar el vehículo con placa ${v.placa}?`)) return;
    this.vehiculoService.eliminarMio(v.id).subscribe({
      next: () => {
        this.toast.success('Vehículo eliminado.');
        this.cargar();
      },
      error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el vehículo.')
    });
  }
}
