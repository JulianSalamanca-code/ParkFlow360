import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { IngresoService } from '../../ingresos/services/ingreso.service';
import { EspacioService } from '../../espacios/services/espacio.service';
import { TarifaService } from '../../tarifas/services/tarifa.service';
import { VehiculoService } from '../../vehiculos/services/vehiculo.service';
import { IngresoRequest } from '../../ingresos/models/ingreso.model';
import { Espacio } from '../../espacios/models/espacio.model';
import { Tarifa } from '../../tarifas/models/tarifa.model';
import { Vehiculo } from '../../vehiculos/models/vehiculo.model';
import { TicketComponent } from '../../../shared/components/ticket/ticket.component';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-reservar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TicketComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Reservar Parqueadero</h1>
          <p>Selecciona tu vehículo y un espacio disponible para reservar</p>
        </div>
      </div>

      <div class="ingreso-grid">
        <div class="form-card" style="max-width:none">
          <form [formGroup]="form" (ngSubmit)="reservar()">
            <div class="form-group">
              <label for="vehiculoId">Mi Vehículo</label>
              <select id="vehiculoId" formControlName="vehiculoId" class="form-control">
                <option value="">Seleccione su vehículo...</option>
                <option *ngFor="let v of vehiculos" [ngValue]="v.id">
                  {{ v.placa }} · {{ v.tipo }} · {{ v.modelo }}
                </option>
              </select>
              <div *ngIf="vehiculos.length === 0" class="field-error">
                Aún no tienes vehículos. <a routerLink="/mis-vehiculos/nuevo">Agrega uno aquí</a>.
              </div>
            </div>

            <div class="form-group">
              <label for="espacioId">Espacio Disponible</label>
              <select id="espacioId" formControlName="espacioId" class="form-control">
                <option value="">Seleccione un espacio...</option>
                <option *ngFor="let e of espaciosLibres" [ngValue]="e.id">
                  {{ e.numero }} · Piso {{ e.piso }} · {{ e.tipo }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="tarifaId">Tarifa</label>
              <select id="tarifaId" formControlName="tarifaId" class="form-control">
                <option value="">Seleccione una tarifa...</option>
                <option *ngFor="let t of tarifas" [ngValue]="t.id">
                  {{ t.nombre }} · {{ t.valor | currency:'COP':'symbol-narrow':'1.0-0' }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="novedad">Observación</label>
              <input id="novedad" type="text" formControlName="novedad" class="form-control"
                     placeholder="Sin novedad" />
            </div>

            <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
                <span class="material-symbols-outlined">event_available</span>
                {{ isLoading ? 'Reservando...' : 'Confirmar Reserva' }}
              </button>
            </div>
          </form>
        </div>

        <div class="ticket-col">
          <div class="ticket-head">
            <span class="material-symbols-outlined">receipt_long</span>
            <strong>Comprobante de Reserva</strong>
            <span class="badge badge-warning" style="margin-left:auto">RESERVA</span>
          </div>

          <app-ticket
            titulo="RESERVA"
            [folio]="folio || 'RSV-PREVIEW'"
            [placa]="placaSeleccionada"
            [categoria]="categoriaSeleccionada"
            [espacio]="espacioLabel"
            [tarifaValor]="tarifaValor"
            [modalidad]="'POR HORAS'"
            [operador]="nombreCliente"
            [novedad]="form.get('novedad')?.value"
            [fecha]="fechaTicket"
          ></app-ticket>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .ingreso-grid {
      display: grid;
      grid-template-columns: 1fr 400px;
      gap: 24px;
      align-items: start;
    }
    .ticket-col { display: flex; flex-direction: column; align-items: center; }
    .ticket-head {
      display: flex; align-items: center; gap: 8px; width: 100%;
      margin-bottom: 12px; font-size: 14px; color: var(--pf-ink);
    }
    .ticket-head .material-symbols-outlined { color: var(--pf-primary); }
    @media (max-width: 1000px) {
      .ingreso-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class ReservarComponent implements OnInit {
  form!: FormGroup;
  vehiculos: Vehiculo[] = [];
  espacios: Espacio[] = [];
  tarifas: Tarifa[] = [];
  folio = '';
  isLoading = false;
  errorMessage = '';
  fechaTicket: string | null = null;
  nombreCliente = '';

  constructor(
    private fb: FormBuilder,
    private ingresoService: IngresoService,
    private espacioService: EspacioService,
    private tarifaService: TarifaService,
    private vehiculoService: VehiculoService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      vehiculoId: [''],
      espacioId: [''],
      tarifaId: [''],
      novedad: ['']
    });

    this.vehiculoService.misVehiculos().subscribe({
      next: (data) => this.vehiculos = data,
      error: (err) => console.error('Error al cargar vehículos:', err)
    });

    this.espacioService.listarTodos().subscribe({
      next: (data) => this.espacios = data,
      error: (err) => console.error('Error al cargar espacios:', err)
    });

    this.tarifaService.listarTodos().subscribe({
      next: (data) => this.tarifas = data,
      error: (err) => console.error('Error al cargar tarifas:', err)
    });
  }

  get espaciosLibres(): Espacio[] {
    return this.espacios.filter(e => e.estado?.toUpperCase() === 'LIBRE');
  }

  get vehiculoSeleccionado(): Vehiculo | undefined {
    const id = this.form?.get('vehiculoId')?.value;
    return this.vehiculos.find(v => v.id === id);
  }

  get placaSeleccionada(): string {
    return this.vehiculoSeleccionado?.placa || '---';
  }

  get categoriaSeleccionada(): string {
    return this.vehiculoSeleccionado?.tipo || 'CARRO';
  }

  get espacioLabel(): string {
    const id = this.form?.get('espacioId')?.value;
    const e = this.espacios.find(x => x.id === id);
    return e ? `${e.numero} (Piso ${e.piso})` : '---';
  }

  get tarifaValor(): number | null {
    const id = this.form?.get('tarifaId')?.value;
    const t = this.tarifas.find(x => x.id === id);
    return t ? Number(t.valor) : null;
  }

  reservar(): void {
    const vehiculo = this.vehiculoSeleccionado;
    const espacioId = this.form.get('espacioId')?.value;

    if (!vehiculo) {
      this.errorMessage = 'Selecciona un vehículo';
      return;
    }
    if (!espacioId) {
      this.errorMessage = 'Selecciona un espacio';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const espacio = this.espacios.find(e => e.id === espacioId);

    const request: IngresoRequest = {
      placa: vehiculo.placa,
      categoria: vehiculo.tipo,
      vehiculoId: vehiculo.id,
      espacioId: espacioId,
      espacioNumero: espacio?.numero,
      tarifaId: this.form.get('tarifaId')?.value || undefined,
      tarifaValor: this.tarifaValor ?? undefined,
      modalidad: 'POR HORAS',
      novedad: this.form.get('novedad')?.value
    };

    this.ingresoService.reservar(request).subscribe({
      next: (ingreso) => {
        this.isLoading = false;
        this.toast.success(`¡Reserva exitosa! Tu comprobante es ${ingreso.folio}.`);
        this.router.navigate(['/mis-parqueos']);
      },
      error: (err) => {
        const msg = err.error?.message || 'No se pudo completar la reserva.';
        this.errorMessage = msg;
        this.toast.error(msg);
        this.isLoading = false;
      }
    });
  }
}
