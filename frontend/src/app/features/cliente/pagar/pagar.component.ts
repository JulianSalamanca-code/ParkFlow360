import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PagoService } from '../../pagos/services/pago.service';
import { IngresoService } from '../../ingresos/services/ingreso.service';
import { Pago } from '../../pagos/models/pago.model';
import { Ingreso } from '../../ingresos/models/ingreso.model';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-pagar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Pagar en Línea</h1>
          <p>Genera el recibo y paga con Wompi (tarjeta, PSE o Nequi)</p>
        </div>
      </div>

      <div class="split">
        <div class="form-card" style="max-width:none">
          <form [formGroup]="form" (ngSubmit)="pagar()">
            <div class="form-header"><h2>Datos del pago</h2></div>

            <div class="form-group">
              <label for="ingresoId">Parqueo a pagar (opcional)</label>
              <select id="ingresoId" formControlName="ingresoId" class="form-control">
                <option value="">Pago libre (sin parqueo asociado)</option>
                <option *ngFor="let i of pendientes" [ngValue]="i.id">
                  {{ i.folio }} · {{ i.placa }} · Espacio {{ i.espacioNumero || i.espacioId }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="valor">Valor a pagar (COP) *</label>
              <input id="valor" type="number" min="100" formControlName="valor" class="form-control" />
            </div>

            <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
                <span class="material-symbols-outlined">payments</span>
                {{ isLoading ? 'Generando recibo...' : 'Pagar con Wompi' }}
              </button>
            </div>
          </form>
        </div>

        <div class="card">
          <div class="card-body">
            <h3 class="section-title" style="margin-top:0">Mis pagos</h3>
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr><th>Referencia</th><th>Valor</th><th>Estado</th><th>Fecha</th></tr>
                </thead>
                <tbody>
                  <tr *ngFor="let p of pagos">
                    <td class="mono">{{ p.referencia || ('#' + p.id) }}</td>
                    <td class="num">{{ p.valor | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
                    <td><span class="badge" [ngClass]="estadoBadge(p.estado)">{{ p.estado }}</span></td>
                    <td class="mono">{{ p.fecha | date:'short' }}</td>
                  </tr>
                  <tr *ngIf="pagos.length === 0">
                    <td colspan="4" class="table-empty">Aún no tienes pagos registrados</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .split { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; align-items: start; }
    @media (max-width: 1000px) { .split { grid-template-columns: 1fr; } }
  `]
})
export class PagarComponent implements OnInit {
  form!: FormGroup;
  pendientes: Ingreso[] = [];
  pagos: Pago[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private pagoService: PagoService,
    private ingresoService: IngresoService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      ingresoId: [''],
      valor: [null, [Validators.required, Validators.min(100)]]
    });

    this.ingresoService.misIngresos().subscribe({
      next: (data) => this.pendientes = data.filter(i => i.estado !== 'FINALIZADO' && i.estado !== 'CANCELADO')
    });

    this.cargarPagos();
  }

  cargarPagos(): void {
    this.pagoService.misPagos().subscribe({
      next: (data) => this.pagos = data,
      error: () => { /* sin pagos aún */ }
    });
  }

  pagar(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.errorMessage = '';

    const { ingresoId, valor } = this.form.value;
    this.pagoService.crearPagoOnline({ valor: Number(valor), ingresoId: ingresoId || undefined }).subscribe({
      next: (resp) => {
        this.isLoading = false;
        if (!resp.checkoutUrl) {
          this.errorMessage = 'Wompi no está configurado. Contacta al administrador.';
          return;
        }
        this.toast.info('Redirigiendo a Wompi...');
        window.location.href = resp.checkoutUrl;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'No se pudo iniciar el pago.';
      }
    });
  }

  estadoBadge(estado?: string): string {
    switch ((estado || '').toUpperCase()) {
      case 'PAGADO': return 'badge-available';
      case 'PENDIENTE': return 'badge-warning';
      case 'RECHAZADO': return 'badge-occupied';
      default: return 'badge-neutral';
    }
  }
}
