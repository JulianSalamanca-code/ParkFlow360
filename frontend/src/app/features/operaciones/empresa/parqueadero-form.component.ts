import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OperacionesService } from '../services/operaciones.service';
import {
  EspacioCrearRequest,
  ParqueaderoCreadoResponse,
  ParqueaderoCrearRequest
} from '../models/operaciones.model';

interface TipoFormValue {
  readonly tipo?: string | null;
  readonly cantidadFija?: boolean | null;
  readonly cantidad?: number | null;
  readonly puestos?: number | null;
}

@Component({
  selector: 'app-parqueadero-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <article class="card card-body crear-parqueadero" data-testid="parqueadero-form">
      <div class="crear-header">
        <div>
          <h2>Nuevo parqueadero</h2>
          <p>Se crean en un solo paso el parqueadero, sus tipos, los puestos y el usuario cajero.</p>
        </div>
        <button type="button" class="btn btn-secondary btn-sm" (click)="cancel.emit()"
                data-testid="pq-cancel">Cancelar</button>
      </div>

      <form [formGroup]="form" (ngSubmit)="guardar()">
        <div class="form-grid">
          <div class="form-group">
            <label for="pq-nombre">Nombre</label>
            <input id="pq-nombre" data-testid="pq-nombre" class="form-control" type="text"
                   formControlName="nombre" maxlength="150" placeholder="Parqueadero Central" />
            <small class="field-error" *ngIf="form.get('nombre')?.invalid && form.get('nombre')?.touched">
              El nombre es obligatorio.
            </small>
          </div>

          <div class="form-group">
            <label for="pq-identificador">Identificador</label>
            <input id="pq-identificador" data-testid="pq-identificador" class="form-control" type="text"
                   formControlName="identificador" maxlength="80" placeholder="CENTRAL-01" />
            <small class="field-error" *ngIf="form.get('identificador')?.invalid && form.get('identificador')?.touched">
              El identificador es obligatorio y debe ser único por empresa.
            </small>
          </div>

          <div class="form-group">
            <label for="pq-capacidad">Capacidad de vehículos (opcional)</label>
            <input id="pq-capacidad" data-testid="pq-capacidad" class="form-control" type="number"
                   min="0" step="1" formControlName="capacidadVehiculos" placeholder="50" />
            <small class="field-error" *ngIf="form.get('capacidadVehiculos')?.invalid">
              La capacidad no puede ser negativa.
            </small>
          </div>
        </div>

        <div class="tipos" formArrayName="tipos">
          <div class="tipos-header">
            <div>
              <h3>Tipos y puestos</h3>
              <p *ngIf="totalPuestos">Se generarán <strong data-testid="pq-total-puestos">{{ totalPuestos }}</strong> puestos numerados automáticamente.</p>
              <p *ngIf="!totalPuestos">Indica cuántos puestos crear por tipo. Si dejas "Puestos" vacío con cupo fijo, se usa el cupo.</p>
            </div>
            <button type="button" class="btn btn-secondary btn-sm" (click)="agregarTipo()"
                    data-testid="pq-agregar-tipo">+ Agregar tipo</button>
          </div>

          <div class="tipo-row" *ngFor="let tipo of tipos.controls; let i = index; trackBy: trackByIndex"
               [formGroupName]="i">
            <input class="form-control" type="text" maxlength="50" formControlName="tipo"
                   [attr.data-testid]="'pq-tipo-' + i" placeholder="CARRO" />
            <label class="check">
              <input type="checkbox" formControlName="cantidadFija"
                     [attr.data-testid]="'pq-cantidad-fija-' + i"
                     (change)="onCantidadFijaChange(i, $event)" />
              Cupo fijo
            </label>
            <input class="form-control" type="number" min="1" step="1" formControlName="cantidad"
                   [attr.data-testid]="'pq-cantidad-' + i" placeholder="Cupo" />
            <input class="form-control" type="number" min="0" max="2000" step="1" formControlName="puestos"
                   [attr.data-testid]="'pq-puestos-' + i" placeholder="Puestos a crear" />
            <button type="button" class="btn btn-secondary btn-sm" (click)="quitarTipo(i)"
                    [disabled]="tipos.length === 1" [attr.data-testid]="'pq-quitar-tipo-' + i">
              Quitar
            </button>
            <small class="field-error"
                   *ngIf="tipos.at(i).get('tipo')?.invalid && tipos.at(i).get('tipo')?.touched">
              Indica el tipo de vehículo.
            </small>
            <small class="field-error"
                   *ngIf="esCantidadFija(i) && tipos.at(i).get('cantidad')?.invalid && tipos.at(i).get('cantidad')?.touched">
              El cupo fijo debe ser mayor a cero.
            </small>
            <small class="field-error"
                   *ngIf="tipos.at(i).get('puestos')?.invalid && tipos.at(i).get('puestos')?.touched">
              El número de puestos debe estar entre 0 y 2000.
            </small>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="pq-cajero-email">Correo del cajero</label>
            <input id="pq-cajero-email" data-testid="pq-cajero-email" class="form-control" type="email"
                   formControlName="cajeroEmail" placeholder="cajero@parkflow360.com" />
            <small class="field-error" *ngIf="form.get('cajeroEmail')?.invalid && form.get('cajeroEmail')?.touched">
              Ingresa un correo válido.
            </small>
          </div>

          <div class="form-group">
            <label for="pq-cajero-password">Contraseña temporal del cajero</label>
            <input id="pq-cajero-password" data-testid="pq-cajero-password" class="form-control" type="password"
                   formControlName="cajeroPassword" placeholder="Mínimo 8 caracteres" />
            <small class="field-error" *ngIf="form.get('cajeroPassword')?.invalid && form.get('cajeroPassword')?.touched">
              Debe tener al menos 8 caracteres.
            </small>
          </div>

          <div class="form-group">
            <label for="pq-cajero-nombre">Nombre del cajero (opcional)</label>
            <input id="pq-cajero-nombre" data-testid="pq-cajero-nombre" class="form-control" type="text"
                   formControlName="cajeroNombre" maxlength="100" placeholder="Nombre del cajero" />
          </div>
        </div>

        <div *ngIf="errorMessage" class="alert alert-danger" data-testid="pq-form-error" role="alert">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button class="btn btn-primary" type="submit" [disabled]="form.invalid || saving || !empresaId"
                  data-testid="pq-submit">
            {{ saving ? 'Creando...' : 'Crear parqueadero' }}
          </button>
        </div>
      </form>
    </article>
  `,
  styles: [`
    .crear-parqueadero { display: grid; gap: 18px; margin-bottom: 18px; }
    .crear-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
    .crear-header h2 { font-size: 17px; }
    .crear-header p, .tipos-header p { color: var(--pf-muted); font-size: 13px; margin: 4px 0 0; }
    .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
    .tipos { display: grid; gap: 10px; border-top: 1px solid var(--pf-outline-soft); padding-top: 14px; }
    .tipos-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; }
    .tipos-header h3 { font-size: 14px; }
    .tipo-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
    .tipo-row .form-control { max-width: 175px; }
    .tipo-row .check { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; }
    .tipo-row .field-error { flex-basis: 100%; }
    .form-actions { display: flex; justify-content: flex-end; }
  `]
})
export class ParqueaderoFormComponent {
  @Input() empresaId?: number;
  @Output() readonly created = new EventEmitter<ParqueaderoCreadoResponse>();
  @Output() readonly cancel = new EventEmitter<void>();

  readonly form: FormGroup;
  saving = false;
  errorMessage = '';

  constructor(private fb: FormBuilder, private operaciones: OperacionesService) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      identificador: ['', [Validators.required, Validators.maxLength(80)]],
      capacidadVehiculos: [null, [Validators.min(0)]],
      tipos: this.fb.array([this.nuevoTipo()]),
      cajeroEmail: ['', [Validators.required, Validators.email]],
      cajeroPassword: ['', [Validators.required, Validators.minLength(8)]],
      cajeroNombre: ['', [Validators.maxLength(100)]]
    });
  }

  get tipos(): FormArray {
    return this.form.get('tipos') as FormArray;
  }

  get totalPuestos(): number {
    return this.buildPuestos().espacios.length;
  }

  trackByIndex(index: number): number {
    return index;
  }

  esCantidadFija(index: number): boolean {
    return Boolean(this.tipos.at(index).get('cantidadFija')?.value);
  }

  agregarTipo(): void {
    this.tipos.push(this.nuevoTipo());
  }

  quitarTipo(index: number): void {
    if (this.tipos.length > 1) {
      this.tipos.removeAt(index);
    }
  }

  onCantidadFijaChange(index: number, event: Event): void {
    const cantidad = this.tipos.at(index).get('cantidad');
    if (!cantidad) {
      return;
    }
    if ((event.target as HTMLInputElement).checked) {
      cantidad.setValidators([Validators.required, Validators.min(1)]);
    } else {
      cantidad.clearValidators();
    }
    cantidad.updateValueAndValidity();
  }

  guardar(): void {
    if (this.form.invalid || this.saving || this.empresaId == null) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;
    const { espacios, error } = this.buildPuestos();
    if (error) {
      this.errorMessage = error;
      return;
    }

    const request: ParqueaderoCrearRequest = {
      nombre: String(value.nombre ?? '').trim(),
      identificador: String(value.identificador ?? '').trim().toUpperCase(),
      capacidadVehiculos: value.capacidadVehiculos ?? null,
      tipos: ((value.tipos ?? []) as TipoFormValue[]).map(tipo => ({
        tipo: String(tipo.tipo ?? '').trim().toUpperCase(),
        cantidadFija: Boolean(tipo.cantidadFija),
        cantidad: tipo.cantidadFija ? tipo.cantidad ?? null : null
      })),
      espacios,
      cajeroEmail: String(value.cajeroEmail ?? '').trim(),
      cajeroPassword: String(value.cajeroPassword ?? ''),
      cajeroNombre: String(value.cajeroNombre ?? '').trim()
    };

    this.saving = true;
    this.errorMessage = '';
    this.operaciones.crearParqueadero(this.empresaId, request).subscribe({
      next: creado => {
        this.saving = false;
        this.created.emit(creado);
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving = false;
        this.errorMessage = err.error?.message
          ?? 'No se pudo crear el parqueadero. Revisa los datos e intenta nuevamente.';
      }
    });
  }

  /** Genera los puestos reales a partir de los tipos configurados. */
  private buildPuestos(): { espacios: EspacioCrearRequest[]; error: string } {
    const tipos = (this.form.value.tipos ?? []) as TipoFormValue[];
    const espacios: EspacioCrearRequest[] = [];
    const vistos = new Set<string>();

    for (const tipo of tipos) {
      const nombre = String(tipo.tipo ?? '').trim().toUpperCase();
      if (!nombre) {
        continue;
      }
      const solicitados = Number(tipo.puestos
        ?? (tipo.cantidadFija ? tipo.cantidad : 0)
        ?? 0);
      if (!Number.isInteger(solicitados) || solicitados < 0) {
        continue;
      }
      const prefijo = nombre.replace(/[^A-Z0-9]/g, '').slice(0, 8) || 'ESP';
      for (let index = 1; index <= solicitados; index++) {
        const numero = `${prefijo}-${String(index).padStart(3, '0')}`;
        if (vistos.has(numero)) {
          return { espacios: [], error: `El número de puesto ${numero} se repite. Ajusta los tipos.` };
        }
        vistos.add(numero);
        espacios.push({ numero, tipo: nombre, piso: 1 });
      }
    }
    if (espacios.length > 2000) {
      return { espacios: [], error: 'No se pueden crear más de 2000 puestos en un solo parqueadero.' };
    }
    return { espacios, error: '' };
  }

  private nuevoTipo(): FormGroup {
    return this.fb.group({
      tipo: ['', [Validators.required, Validators.maxLength(50)]],
      cantidadFija: [false],
      cantidad: [null],
      puestos: [null, [Validators.min(0), Validators.max(2000)]]
    });
  }
}
