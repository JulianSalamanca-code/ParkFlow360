import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IngresoService } from '../../ingresos/services/ingreso.service';
import { Ingreso } from '../../ingresos/models/ingreso.model';
import { TicketComponent } from '../../../shared/components/ticket/ticket.component';

@Component({
  selector: 'app-mis-parqueos',
  standalone: true,
  imports: [CommonModule, RouterLink, TicketComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Mis Parqueos</h1>
          <p>Historial de tus reservas y estancias · selecciona uno para ver su comprobante</p>
        </div>
        <a routerLink="/reservar" class="btn btn-primary">
          <span class="material-symbols-outlined">add_circle</span> Reservar Parqueadero
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Total</div>
          <div class="value">{{ parkings.length }}</div>
        </div>
        <div class="metric metric-warning">
          <div class="label">Reservados</div>
          <div class="value">{{ contarPorEstado('RESERVADO') }}</div>
        </div>
        <div class="metric metric-occupied">
          <div class="label">Activos</div>
          <div class="value">{{ contarPorEstado('ACTIVO') }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Finalizados</div>
          <div class="value">{{ contarPorEstado('FINALIZADO') }}</div>
        </div>
      </div>

      <div class="split">
        <div class="card">
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>Folio</th>
                  <th>Placa</th>
                  <th>Espacio</th>
                  <th>Estado</th>
                  <th>Entrada</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let p of parkings"
                    class="row-click" [class.selected]="seleccionado?.id === p.id"
                    (click)="seleccionado = p">
                  <td><span class="plate">{{ p.folio }}</span></td>
                  <td><span class="plate">{{ p.placa }}</span></td>
                  <td class="num">{{ p.espacioNumero || p.espacioId }}</td>
                  <td><span class="badge" [ngClass]="estadoBadge(p.estado)">{{ p.estado }}</span></td>
                  <td class="mono">{{ p.fechaEntrada | date:'short' }}</td>
                </tr>
                <tr *ngIf="parkings.length === 0">
                  <td colspan="5" class="table-empty">No tienes parqueos registrados</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="ticket-col">
          <div class="ticket-head">
            <span class="material-symbols-outlined">receipt_long</span>
            <strong>Comprobante</strong>
            <button *ngIf="seleccionado" class="btn btn-sm btn-secondary" style="margin-left:auto" (click)="imprimir()">
              <span class="material-symbols-outlined">print</span> Imprimir
            </button>
          </div>

          <app-ticket
            *ngIf="seleccionado"
            [titulo]="seleccionado.estado === 'RESERVADO' ? 'RESERVA' : 'TICKET INGRESO'"
            [folio]="seleccionado.folio"
            [placa]="seleccionado.placa"
            [categoria]="seleccionado.categoria"
            [espacio]="espacioTexto(seleccionado)"
            [tarifaValor]="seleccionado.tarifaValor ?? null"
            [modalidad]="seleccionado.modalidad || ''"
            [operador]="seleccionado.operador || ''"
            [novedad]="seleccionado.novedad || ''"
            [fecha]="seleccionado.fechaEntrada">
          </app-ticket>

          <div *ngIf="!seleccionado" class="ticket-placeholder">
            <span class="material-symbols-outlined">receipt_long</span>
            <p>Selecciona un parqueo para ver su comprobante.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .split { display: grid; grid-template-columns: 1fr 400px; gap: 20px; align-items: start; }
    .row-click { cursor: pointer; }
    .row-click.selected { background: var(--pf-primary-container) !important; }
    .ticket-col { display: flex; flex-direction: column; align-items: center; }
    .ticket-head {
      display: flex; align-items: center; gap: 8px; width: 100%;
      margin-bottom: 12px; font-size: 14px;
    }
    .ticket-head .material-symbols-outlined { color: var(--pf-primary); }
    .ticket-placeholder {
      width: 100%; max-width: 360px; min-height: 320px;
      border: 2px dashed var(--pf-outline); border-radius: var(--pf-radius);
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 10px; color: var(--pf-muted); text-align: center; padding: 24px;
    }
    .ticket-placeholder .material-symbols-outlined { font-size: 40px; }
    @media (max-width: 1100px) { .split { grid-template-columns: 1fr; } }
  `]
})
export class MisParqueosComponent implements OnInit {
  parkings: Ingreso[] = [];
  seleccionado: Ingreso | null = null;

  constructor(private ingresoService: IngresoService) {}

  ngOnInit(): void {
    this.ingresoService.misIngresos().subscribe({
      next: (data) => this.parkings = data,
      error: (err) => console.error('Error al cargar mis parqueos:', err)
    });
  }

  espacioTexto(ingreso: Ingreso): string {
    return ingreso.espacioNumero || String(ingreso.espacioId || '---');
  }

  contarPorEstado(estado: string): number {
    return this.parkings.filter(p => p.estado?.toUpperCase() === estado).length;
  }

  estadoBadge(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'ACTIVO': return 'badge-occupied';
      case 'RESERVADO': return 'badge-warning';
      case 'FINALIZADO': return 'badge-available';
      default: return 'badge-neutral';
    }
  }

  imprimir(): void {
    window.print();
  }
}
