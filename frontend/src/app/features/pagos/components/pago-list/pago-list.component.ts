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
          <h1>Pagos</h1>
          <p>Historial de transacciones y recaudo del parqueadero</p>
        </div>
        <a routerLink="/pagos/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Nuevo Pago
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Transacciones</div>
          <div class="value">{{ pagos.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Recaudo Total</div>
          <div class="value">{{ recaudoTotal | currency:'COP':'symbol-narrow':'1.0-0' }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Ticket</th>
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
                <td><span class="plate">#{{ pago.id }}</span></td>
                <td class="num">{{ pago.vehiculoId }}</td>
                <td class="num">{{ pago.espacioId }}</td>
                <td class="num">{{ pago.tarifaId }}</td>
                <td><span class="num">{{ pago.valor | currency:'COP':'symbol-narrow':'1.0-0' }}</span></td>
                <td class="mono">{{ pago.fecha | date:'short' }}</td>
                <td><span class="badge badge-neutral">{{ pago.metodoPago }}</span></td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/pagos/editar', pago.id]" class="btn btn-sm btn-secondary">
                      <span class="material-symbols-outlined">edit</span>
                    </a>
                    <button (click)="eliminar(pago.id)" class="btn btn-sm btn-danger">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
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

  get recaudoTotal(): number {
    return this.pagos.reduce((sum, p) => sum + (Number(p.valor) || 0), 0);
  }

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
