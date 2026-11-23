import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { OperacionesService } from '../services/operaciones.service';
import { PagoOperativo } from '../models/operaciones.model';

type CajeroView = 'pendientes' | 'historial';

@Component({
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>{{ view === 'pendientes' ? 'Pagos pendientes' : 'Historial de pagos' }}</h1>
          <p>{{ view === 'pendientes' ? 'Revisa y confirma los cobros del turno.' : 'Últimos pagos confirmados en este parqueadero.' }}</p>
        </div>
        <span class="badge badge-neutral" data-testid="cajero-conteo">{{ pagos.length }} {{ pagos.length === 1 ? 'registro' : 'registros' }}</span>
      </header>

      <div *ngIf="successMessage" class="alert alert-success" data-testid="cajero-success" role="status">{{ successMessage }}</div>
      <div *ngIf="loading" class="card state" data-testid="cajero-loading" role="status">
        <span class="material-symbols-outlined">progress_activity</span> Cargando pagos...
      </div>
      <div *ngIf="!loading && errorMessage" class="alert alert-danger" data-testid="cajero-error" role="alert">
        <span>{{ errorMessage }}</span>
        <button class="btn btn-secondary btn-sm" type="button" (click)="load()">Reintentar</button>
      </div>

      <div *ngIf="!loading && !errorMessage && pagos.length" class="card table-wrap">
        <table class="table" [attr.data-testid]="view === 'pendientes' ? 'cajero-pagos' : 'cajero-historial'">
          <thead><tr><th>Referencia</th><th>Vehículo</th><th>Cliente</th><th>Hora</th><th>Valor</th><th>Estado / acción</th></tr></thead>
          <tbody>
            <tr *ngFor="let pago of pagos">
              <td class="mono">{{ pago.referencia }}</td>
              <td class="plate">{{ pago.placa }}</td>
              <td>{{ pago.cliente }}</td>
              <td>{{ pago.fecha }}</td>
              <td>{{ pago.monto | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
              <td>
                <button *ngIf="view === 'pendientes'" class="btn btn-primary btn-sm"
                        type="button" [disabled]="confirmingId === pago.id"
                        (click)="confirmar(pago)" [attr.data-testid]="'cajero-confirmar-' + pago.id">
                  {{ confirmingId === pago.id ? 'Confirmando...' : 'Confirmar cobro' }}
                </button>
                <span *ngIf="view === 'historial'" class="badge badge-available">Confirmado</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div *ngIf="!loading && !errorMessage && !pagos.length" class="card state empty" data-testid="cajero-empty">
        <span class="material-symbols-outlined">task_alt</span>
        <h2>{{ view === 'pendientes' ? 'Todo al día' : 'Aún no hay pagos registrados' }}</h2>
        <p>{{ view === 'pendientes' ? 'No tienes cobros pendientes por confirmar.' : 'Los pagos confirmados aparecerán aquí.' }}</p>
      </div>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [`
    .page { max-width: 1180px; margin: 0 auto; }
    .state { min-height: 180px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; text-align: center; padding: 28px; }
    .state .material-symbols-outlined { font-size: 30px; color: var(--pf-muted); }
    .state h2 { font-size: 17px; }
    .state p { color: var(--pf-muted); }
    .empty { border-style: dashed; }
    .alert { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .alert-success { background: var(--pf-available-bg); border-color: var(--pf-available-border); color: var(--pf-available-text); }
  `]
})
export class CajeroComponent {
  readonly view: CajeroView;
  pagos: PagoOperativo[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';
  confirmingId: number | null = null;

  constructor(route: ActivatedRoute, private operaciones: OperacionesService) {
    this.view = route.snapshot.data['view'] as CajeroView;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    const request = this.view === 'pendientes'
      ? this.operaciones.getPagosPendientes()
      : this.operaciones.getHistorialPagos();
    request.subscribe({
      next: pagos => { this.pagos = pagos; this.loading = false; },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de pagos. Verifica tu vínculo con el parqueadero.';
      }
    });
  }

  confirmar(pago: PagoOperativo): void {
    this.confirmingId = pago.id;
    this.successMessage = '';
    this.errorMessage = '';
    this.operaciones.confirmarPago(pago.id).subscribe({
      next: () => {
        this.confirmingId = null;
        this.successMessage = `Cobro ${pago.referencia} confirmado.`;
        this.load();
      },
      error: () => {
        this.confirmingId = null;
        this.errorMessage = 'No se pudo confirmar el cobro. Verifica tus permisos y el estado de la factura.';
      }
    });
  }
}
