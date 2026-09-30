import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PagoService } from '../../services/pago.service';
import { Pago } from '../../models/pago.model';

@Component({
  selector: 'app-pago-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="pago-list">
      <div class="header">
        <h2>Pagos</h2>
        <a routerLink="/pagos/nuevo" class="btn btn-primary">Nuevo Pago</a>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Vehículo ID</th>
            <th>Espacio ID</th>
            <th>Tarifa ID</th>
            <th>Valor</th>
            <th>Fecha</th>
            <th>Método</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let pago of pagos">
            <td>{{ pago.id }}</td>
            <td>{{ pago.vehiculoId }}</td>
            <td>{{ pago.espacioId }}</td>
            <td>{{ pago.tarifaId }}</td>
            <td>{{ pago.valor | currency }}</td>
            <td>{{ pago.fecha | date:'short' }}</td>
            <td>{{ pago.metodoPago }}</td>
            <td>
              <a [routerLink]="['/pagos/editar', pago.id]" class="btn btn-sm btn-warning">Editar</a>
              <button (click)="eliminar(pago.id)" class="btn btn-sm btn-danger">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .pago-list { padding: 20px; }
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
export class PagoListComponent implements OnInit {
  pagos: Pago[] = [];

  constructor(private pagoService: PagoService) {}

  ngOnInit(): void {
    this.cargarPagos();
  }

  cargarPagos(): void {
    this.pagoService.listarTodos().subscribe({
      next: (data) => this.pagos = data,
      error: (err) => console.error('Error al cargar pagos:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este pago?')) {
      this.pagoService.eliminar(id).subscribe({
        next: () => this.cargarPagos(),
        error: (err) => console.error('Error al eliminar:', err)
      });
    }
  }
}
