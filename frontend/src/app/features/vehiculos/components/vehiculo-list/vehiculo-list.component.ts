import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VehiculoService } from '../../services/vehiculo.service';
import { Vehiculo } from '../../models/vehiculo.model';

@Component({
  selector: 'app-vehiculo-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <div>
            <h1>Vehículos</h1>
            <p class="page-subtitle">Registro y control de vehículos del parqueadero</p>
          </div>
        </div>
        <a routerLink="/vehiculos/nuevo" class="btn btn-primary">+ Nuevo Vehículo</a>
      </div>

      <div class="card">
        <div class="table-wrapper">
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
                <td><strong>{{ vehiculo.placa }}</strong></td>
                <td>{{ vehiculo.tipo }}</td>
                <td>{{ vehiculo.color }}</td>
                <td>{{ vehiculo.modelo }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/vehiculos/editar', vehiculo.id]" class="btn btn-sm btn-warning">Editar</a>
                    <button (click)="eliminar(vehiculo.id)" class="btn btn-sm btn-danger">Eliminar</button>
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

  constructor(private vehiculoService: VehiculoService) {}

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.vehiculoService.listarTodos().subscribe({
      next: (data) => this.vehiculos = data,
      error: (err) => console.error('Error al cargar vehículos:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este vehículo?')) {
      this.vehiculoService.eliminar(id).subscribe({
        next: () => this.cargarVehiculos(),
        error: (err) => console.error('Error al eliminar:', err)
      });
    }
  }
}
