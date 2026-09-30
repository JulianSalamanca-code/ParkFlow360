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
    <div class="vehiculo-list">
      <div class="header">
        <h2>Vehículos</h2>
        <a routerLink="/vehiculos/nuevo" class="btn btn-primary">Nuevo Vehículo</a>
      </div>

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
            <td>{{ vehiculo.placa }}</td>
            <td>{{ vehiculo.tipo }}</td>
            <td>{{ vehiculo.color }}</td>
            <td>{{ vehiculo.modelo }}</td>
            <td>
              <a [routerLink]="['/vehiculos/editar', vehiculo.id]" class="btn btn-sm btn-warning">Editar</a>
              <button (click)="eliminar(vehiculo.id)" class="btn btn-sm btn-danger">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .vehiculo-list { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    .table th { background-color: #f5f5f5; font-weight: bold; }
    .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-warning { background-color: #ffc107; color: black; }
    .btn-danger { background-color: #dc3545; color: white; }
    .btn-sm { padding: 4px 8px; font-size: 12px; margin-right: 4px; }
  `]
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
