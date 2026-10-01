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
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <div>
            <h1>Pagos</h1>
            <p class="page-subtitle">Historial de transacciones y recaudos</p>
          </div>
        </div>
        <a routerLink="/pagos/nuevo" class="btn btn-primary">+ Nuevo Pago</a>
      </div>

      <div class="card">
        <div class="table-wrapper">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Vehículo</th>
                <th>Espacio</th>
                <th>Tarifa</th>
                <th>Valor</th>
                <th>Fecha</th>
                <th>Método</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let pago of pagos">
                <td><strong>#{{ pago.id }}</strong></td>
                <td>{{ pago.vehiculoId }}</td>
                <td>{{ pago.espacioId }}</td>
                <td>{{ pago.tarifaId }}</td>
                <td><strong>{{ pago.valor | currency:'COP':'symbol-narrow':'1.0-0' }}</strong></td>
                <td>{{ pago.fecha | date:'short' }}</td>
                <td>{{ pago.metodoPago }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/pagos/editar', pago.id]" class="btn btn-sm btn-warning">Editar</a>
                    <button (click)="eliminar(pago.id)" class="btn btn-sm btn-danger">Eliminar</button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="pagos.length === 0">
                <td colspan="8" class="table-empty">No hay pagos registrados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
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
