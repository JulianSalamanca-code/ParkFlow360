import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PagoService } from '../../services/pago.service';
import { PagoRequest } from '../../models/pago.model';
import { IngresoService } from '../../../ingresos/services/ingreso.service';
import { Ingreso } from '../../../ingresos/models/ingreso.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-pago-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ esEdicion ? 'Editar' : 'Registrar pago' }}</h1>
          <p>Asocia el recaudo con una estancia activa antes de registrar la salida.</p>
        </div>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="guardar()" [attr.data-testid]="'pago-form'">
          <div *ngIf="!esEdicion" class="form-group">
            <label for="ingresoId">Estancia activa</label>
            <select id="ingresoId" formControlName="ingresoId" class="form-control"
                    (change)="seleccionarIngreso()" data-testid="select-ingreso">
              <option value="">Seleccione un ingreso...</option>
              <option *ngFor="let ingreso of ingresosActivos" [ngValue]="ingreso.id">
                {{ ingreso.folio }} · {{ ingreso.placa }} · Espacio {{ ingreso.espacioNumero || ingreso.espacioId }}
              </option>
            </select>
            <div *ngIf="form.get('ingresoId')?.invalid && form.get('ingresoId')?.touched"
                 class="field-error" data-testid="error-ingreso">
              Selecciona la estancia a la que corresponde este pago.
            </div>
          </div>

          <div *ngIf="esEdicion && pagoIngresoId" class="form-group linked-entry">
            <span>Estancia asociada</span>
            <strong>{{ ingresoAsociadoLabel }}</strong>
          </div>

          <ng-container *ngIf="esEdicion && !pagoIngresoId">
            <p class="help-text">Pago histórico sin ingreso asociado. Puedes editar sus referencias existentes.</p>
            <div class="form-group">
              <label for="vehiculoId">Vehículo (ID)</label>
              <input id="vehiculoId" type="number" formControlName="vehiculoId" class="form-control mono-input"
                     data-testid="input-vehiculo-id" />
            </div>
            <div class="form-group">
              <label for="espacioId">Espacio (ID)</label>
              <input id="espacioId" type="number" formControlName="espacioId" class="form-control mono-input"
                     data-testid="input-espacio-id" />
            </div>
            <div class="form-group">
              <label for="tarifaId">Tarifa (ID)</label>
              <input id="tarifaId" type="number" formControlName="tarifaId" class="form-control mono-input"
                     data-testid="input-tarifa-id" />
            </div>
          </ng-container>

          <div class="form-group">
            <label for="valor">Valor confirmado</label>
            <input id="valor" type="number" formControlName="valor" class="form-control mono-input"
                   step="0.01" min="0.01" placeholder="2000" data-testid="input-valor" />
            <div *ngIf="form.get('valor')?.invalid && form.get('valor')?.touched"
                 class="field-error" data-testid="error-valor">
              El valor debe ser mayor que cero.
            </div>
          </div>

          <div class="form-group">
            <label for="metodoPago">Método de pago</label>
            <select id="metodoPago" formControlName="metodoPago" class="form-control"
                    data-testid="select-metodo-pago">
              <option value="">Seleccione...</option>
              <option value="EFECTIVO">Efectivo</option>
              <option value="TARJETA">Tarjeta</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="QR">QR</option>
            </select>
            <div *ngIf="form.get('metodoPago')?.invalid && form.get('metodoPago')?.touched"
                 class="field-error" data-testid="error-metodo-pago">
              Selecciona un método de pago.
            </div>
          </div>

          <div class="form-group">
            <label for="estadoPago">Resultado del pago</label>
            <select id="estadoPago" formControlName="estadoPago" class="form-control"
                    data-testid="select-estado-pago">
              <option value="">Seleccione...</option>
              <option value="CONFIRMADO">Confirmado</option>
              <option value="FALLIDO">Fallido</option>
            </select>
            <div *ngIf="form.get('estadoPago')?.invalid && form.get('estadoPago')?.touched"
                 class="field-error" data-testid="error-estado-pago">
              Selecciona el resultado del pago.
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isSaving"
                    data-testid="btn-guardar">
              <span class="material-symbols-outlined">save</span>
              {{ isSaving ? 'Registrando...' : 'Confirmar pago' }}
            </button>
            <a [routerLink]="esEdicion ? '/pagos' : '/ingresos'" class="btn btn-secondary"
               data-testid="btn-cancelar">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .linked-entry { padding: 12px; background: var(--pf-surface-alt); border-radius: var(--pf-radius); }
    .help-text { color: var(--pf-muted); font-size: 13px; }
  `]
})
export class PagoFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  pagoId?: number;
  pagoIngresoId?: number;
  ingresosActivos: Ingreso[] = [];
  isSaving = false;
  private returnToIngressos = false;

  constructor(
    private fb: FormBuilder,
    private pagoService: PagoService,
    private ingresoService: IngresoService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  get ingresoAsociadoLabel(): string {
    const ingreso = this.ingresosActivos.find(item => item.id === this.pagoIngresoId);
    return ingreso ? `${ingreso.folio} · ${ingreso.placa}` : `Ingreso #${this.pagoIngresoId}`;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const ingresoId = this.route.snapshot.queryParamMap.get('ingresoId');
    this.esEdicion = !!id;
    this.returnToIngressos = !!ingresoId && !id;
    this.form = this.fb.group({
      ingresoId: [ingresoId ? Number(ingresoId) : '', this.esEdicion ? [] : [Validators.required]],
      vehiculoId: [''],
      espacioId: [''],
      tarifaId: [''],
      valor: ['', [Validators.required, Validators.min(0.01)]],
      metodoPago: ['', [Validators.required, Validators.maxLength(50)]],
      estadoPago: ['', [Validators.required]]
    });

    this.cargarIngresosActivos();
    if (id) {
      this.pagoId = Number(id);
      this.cargarPago(this.pagoId);
    }
  }

  cargarIngresosActivos(): void {
    this.ingresoService.listarActivos().subscribe({
      next: (ingresos) => {
        this.ingresosActivos = ingresos;
        const queryIngresoId = this.route.snapshot.queryParamMap.get('ingresoId');
        if (queryIngresoId) {
          this.form.patchValue({ ingresoId: Number(queryIngresoId) });
          this.seleccionarIngreso();
        }
      },
      error: () => this.toast.error('No se pudieron cargar las estancias activas.')
    });
  }

  seleccionarIngreso(): void {
    const id = Number(this.form.get('ingresoId')?.value);
    const ingreso = this.ingresosActivos.find(item => item.id === id);
    if (ingreso?.tarifaValor && ingreso.tarifaValor > 0) {
      this.form.patchValue({ valor: ingreso.tarifaValor });
    }
  }

  cargarPago(id: number): void {
    this.pagoService.obtenerPorId(id).subscribe({
      next: (pago) => {
        this.pagoIngresoId = pago.ingresoId || undefined;
        this.form.patchValue({
          ingresoId: pago.ingresoId || '',
          vehiculoId: pago.vehiculoId,
          espacioId: pago.espacioId,
          tarifaId: pago.tarifaId,
          valor: pago.valor,
          metodoPago: pago.metodoPago,
          estadoPago: pago.estadoPago || 'CONFIRMADO'
        });
        if (!this.pagoIngresoId) {
          ['vehiculoId', 'espacioId', 'tarifaId'].forEach(field => {
            const control = this.form.get(field);
            control?.addValidators(Validators.required);
            control?.updateValueAndValidity();
          });
        }
      },
      error: (error) => this.toast.error(error.error?.message || 'No se pudo cargar el pago.')
    });
  }

  guardar(): void {
    if (this.form.invalid || this.isSaving) return;
    const values = this.form.getRawValue();
    const request: PagoRequest = {
      ingresoId: this.pagoIngresoId || Number(values.ingresoId) || undefined,
      valor: Number(values.valor),
      metodoPago: values.metodoPago,
      estadoPago: values.estadoPago
    };

    if (this.esEdicion && !this.pagoIngresoId) {
      request.vehiculoId = Number(values.vehiculoId);
      request.espacioId = Number(values.espacioId);
      request.tarifaId = Number(values.tarifaId);
    }

    this.isSaving = true;
    const save = this.esEdicion && this.pagoId
      ? this.pagoService.actualizar(this.pagoId, request)
      : this.pagoService.crear(request);
    save.subscribe({
      next: () => {
        this.toast.success(`Pago ${this.esEdicion ? 'actualizado' : 'registrado'} correctamente.`);
        this.router.navigate([this.returnToIngressos ? '/ingresos' : '/pagos']);
      },
      error: (error) => {
        this.toast.error(error.error?.message || 'No se pudo guardar el pago.');
        this.isSaving = false;
      }
    });
  }
}
