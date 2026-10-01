import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VehiculoService } from '../../../../vehiculos/services/vehiculo.service';
import { Vehiculo } from '../../../../vehiculos/models/vehiculo.model';

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
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let v of vehiculos">
                <td><span class="plate">{{ v.placa }}</span></td>
                <td><span class="badge badge-neutral">{{ v.tipo }}</span></td>
                <td>{{ v.color }}</td>
                <td>{{ v.modelo }}</td>
              </tr>
              <tr *ngIf="vehiculos.length === 0">
                <td colspan="4" class="table-empty">Aún no tienes vehículos registrados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class MisVehiculosListComponent implements OnInit {
  vehiculos: Vehiculo[] = [];

  constructor(private vehiculoService: VehiculoService) {}

  ngOnInit(): void {
    this.vehiculoService.misVehiculos().subscribe({
      next: (data) => this.vehiculos = data,
      error: (err) => console.error('Error al cargar mis vehículos:', err)
    });
  }
}
