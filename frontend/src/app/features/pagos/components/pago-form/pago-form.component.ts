import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PagoService } from '../../services/pago.service';
import { PagoRequest } from '../../models/pago.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-pago-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ esEdicion ? 'Editar' : 'Nuevo' }} Pago</h1>
          <p>Registra la transacción y el método de recaudo</p>
        </div>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-group">
            <label for="vehiculoId">Vehículo (ID)</label>
            <input id="vehiculoId" type="number" formControlName="vehiculoId" class="form-control mono-input" placeholder="1" />
            <div *ngIf="form.get('vehiculoId')?.invalid && form.get('vehiculoId')?.touched" class="field-error">
              El vehículo es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="espacioId">Espacio (ID)</label>
            <input id="espacioId" type="number" formControlName="espacioId" class="form-control mono-input" placeholder="1" />
            <div *ngIf="form.get('espacioId')?.invalid && form.get('espacioId')?.touched" class="field-error">
              El espacio es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="tarifaId">Tarifa (ID)</label>
            <input id="tarifaId" type="number" formControlName="tarifaId" class="form-control mono-input" placeholder="1" />
            <div *ngIf="form.get('tarifaId')?.invalid && form.get('tarifaId')?.touched" class="field-error">
              La tarifa es obligatoria
            </div>
          </div>

          <div class="form-group">
            <label for="valor">Valor</label>
            <input id="valor" type="number" formControlName="valor" class="form-control mono-input" step="0.01" min="0" placeholder="2000" />
            <div *ngIf="form.get('valor')?.invalid && form.get('valor')?.touched" class="field-error">
              El valor es obligatorio y debe ser positivo
            </div>
          </div>

          <div class="form-group">
            <label for="metodoPago">Método de Pago</label>
            <select id="metodoPago" formControlName="metodoPago" class="form-control">
              <option value="">Seleccione...</option>
              <option value="EFECTIVO">Efectivo</option>
              <option value="TARJETA">Tarjeta</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="QR">QR</option>
            </select>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
              <span class="material-symbols-outlined">save</span> Guardar
            </button>
            <a routerLink="/pagos" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class PagoFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  pagoId?: number;

  constructor(
    private fb: FormBuilder,
    private pagoService: PagoService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      vehiculoId: ['', [Validators.required]],
      espacioId: ['', [Validators.required]],
      tarifaId: ['', [Validators.required]],
      valor: ['', [Validators.required, Validators.min(0)]],
      metodoPago: ['', [Validators.maxLength(50)]]
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.pagoId = +id;
      this.cargarPago(this.pagoId);
    }
  }

  cargarPago(id: number): void {
    this.pagoService.obtenerPorId(id).subscribe({
      next: (pago) => {
        this.form.patchValue({
          vehiculoId: pago.vehiculoId,
          espacioId: pago.espacioId,
          tarifaId: pago.tarifaId,
          valor: pago.valor,
          metodoPago: pago.metodoPago
        });
      },
      error: (err) => console.error('Error al cargar pago:', err)
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const request: PagoRequest = this.form.value;
    const accion = this.esEdicion ? 'actualizado' : 'registrado';

    if (this.esEdicion && this.pagoId) {
      this.pagoService.actualizar(this.pagoId, request).subscribe({
        next: () => {
          this.toast.success(`Pago ${accion} correctamente.`);
          this.router.navigate(['/pagos']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar el pago.')
      });
    } else {
      this.pagoService.crear(request).subscribe({
        next: () => {
          this.toast.success(`Pago ${accion} correctamente.`);
          this.router.navigate(['/pagos']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar el pago.')
      });
    }
  }
}
