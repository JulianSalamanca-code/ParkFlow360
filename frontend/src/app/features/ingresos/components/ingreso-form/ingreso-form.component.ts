import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IngresoService } from '../../services/ingreso.service';
import { EspacioService } from '../../../espacios/services/espacio.service';
import { TarifaService } from '../../../tarifas/services/tarifa.service';
import { IngresoRequest } from '../../models/ingreso.model';
import { Espacio } from '../../../espacios/models/espacio.model';
import { Tarifa } from '../../../tarifas/models/tarifa.model';
import { TicketComponent } from '../../../../shared/components/ticket/ticket.component';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-ingreso-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TicketComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Registro de Entrada</h1>
          <p>Registra el ingreso del vehículo y genera el ticket de parqueo</p>
        </div>
        <a routerLink="/ingresos" class="btn btn-secondary">
          <span class="material-symbols-outlined">arrow_back</span> Volver
        </a>
      </div>

      <div class="ingreso-grid">
        <!-- Formulario -->
        <div class="form-card" style="max-width:none">
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <div class="form-group">
              <label>Categoría del Vehículo</label>
              <div class="cat-row">
                <button type="button" class="cat-btn" [class.selected]="form.get('categoria')?.value === 'CARRO'"
                        (click)="setCategoria('CARRO')">
                  <span class="material-symbols-outlined">directions_car</span> Automóvil
                </button>
                <button type="button" class="cat-btn" [class.selected]="form.get('categoria')?.value === 'MOTO'"
                        (click)="setCategoria('MOTO')">
                  <span class="material-symbols-outlined">two_wheeler</span> Motocicleta
                </button>
              </div>
            </div>

            <div class="form-group">
              <label for="placa">Placa / Patente</label>
              <input id="placa" type="text" formControlName="placa" class="form-control mono-input"
                     placeholder="ABC123" style="font-size:18px;text-align:center;letter-spacing:0.15em" />
              <div *ngIf="form.get('placa')?.invalid && form.get('placa')?.touched" class="field-error">
                La placa es obligatoria
              </div>
            </div>

            <div class="form-group">
              <label for="espacioId">Espacio Asignado</label>
              <select id="espacioId" formControlName="espacioId" class="form-control">
                <option value="">Seleccione un espacio...</option>
                <option *ngFor="let e of espacios" [ngValue]="e.id">
                  {{ e.numero }} · Piso {{ e.piso }} · {{ e.estado }}
                </option>
              </select>
              <div *ngIf="form.get('espacioId')?.invalid && form.get('espacioId')?.touched" class="field-error">
                El espacio es obligatorio
              </div>
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
              <label for="novedad">Novedad / Observación</label>
              <input id="novedad" type="text" formControlName="novedad" class="form-control"
                     placeholder="Sin novedad visible" />
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
                <span class="material-symbols-outlined">lock_open</span>
                {{ isLoading ? 'Registrando...' : 'Confirmar y Abrir Barrera' }}
              </button>
              <a routerLink="/ingresos" class="btn btn-secondary">Cancelar</a>
            </div>
          </form>
        </div>

        <!-- Vista previa del ticket -->
        <div class="ticket-col">
          <div class="ticket-head">
            <span class="material-symbols-outlined">receipt_long</span>
            <strong>Vista Previa Térmica 80mm</strong>
            <span class="badge badge-available" style="margin-left:auto">ESC/POS</span>
          </div>

          <app-ticket
            [titulo]="folio ? 'TICKET INGRESO' : 'PREVISUALIZACIÓN'"
            [folio]="folio || 'TK-PREVIEW'"
            [placa]="form.get('placa')?.value"
            [categoria]="form.get('categoria')?.value"
            [espacio]="espacioLabel"
            [tarifaValor]="tarifaValor"
            [modalidad]="'POR HORAS'"
            [operador]="operador"
            [novedad]="form.get('novedad')?.value"
            [fecha]="fechaTicket"
          ></app-ticket>

          <button *ngIf="folio" type="button" class="btn btn-primary" style="margin-top:14px;width:100%"
                  (click)="imprimir()">
            <span class="material-symbols-outlined">print</span> Imprimir Ticket
          </button>
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
    .cat-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .cat-btn {
      display: flex; align-items: center; justify-content: center; gap: 8px;
      padding: 14px; border: 2px solid var(--pf-outline-soft);
      border-radius: var(--pf-radius); background: #fff; cursor: pointer;
      font-weight: 700; font-size: 14px; color: var(--pf-ink-soft);
      transition: all 0.15s;
    }
    .cat-btn:hover { border-color: var(--pf-primary); }
    .cat-btn.selected {
      border-color: var(--pf-primary); background: var(--pf-primary);
      color: #fff;
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
export class IngresoFormComponent implements OnInit {
  form!: FormGroup;
  espacios: Espacio[] = [];
  tarifas: Tarifa[] = [];
  folio = '';
  isLoading = false;
  operador = '';
  fechaTicket: string | null = null;

  constructor(
    private fb: FormBuilder,
    private ingresoService: IngresoService,
    private espacioService: EspacioService,
    private tarifaService: TarifaService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      categoria: ['CARRO', [Validators.required]],
      placa: ['', [Validators.required, Validators.maxLength(20)]],
      espacioId: ['', [Validators.required]],
      tarifaId: [''],
      novedad: ['', [Validators.maxLength(200)]]
    });

    this.cargarEspacios();
    this.cargarTarifas();
  }

  setCategoria(cat: string): void {
    this.form.get('categoria')?.setValue(cat);
  }

  cargarEspacios(): void {
    this.espacioService.listarTodos().subscribe({
      next: (data) => this.espacios = data,
      error: (err) => console.error('Error al cargar espacios:', err)
    });
  }

  cargarTarifas(): void {
    this.tarifaService.listarTodos().subscribe({
      next: (data) => this.tarifas = data,
      error: (err) => console.error('Error al cargar tarifas:', err)
    });
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

  guardar(): void {
    if (this.form.invalid) return;

    this.isLoading = true;

    const espacioId = this.form.get('espacioId')?.value;
    const espacio = this.espacios.find(e => e.id === espacioId);

    const request: IngresoRequest = {
      placa: this.form.get('placa')?.value,
      categoria: this.form.get('categoria')?.value,
      espacioId: espacioId,
      espacioNumero: espacio?.numero,
      tarifaId: this.form.get('tarifaId')?.value || undefined,
      tarifaValor: this.tarifaValor ?? undefined,
      modalidad: 'POR HORAS',
      novedad: this.form.get('novedad')?.value
    };

    this.ingresoService.crear(request).subscribe({
      next: (ingreso) => {
        this.folio = ingreso.folio;
        this.operador = ingreso.operador || 'OPERADOR';
        this.fechaTicket = ingreso.fechaEntrada;
        this.isLoading = false;
        this.toast.success(`Ingreso registrado. Ticket ${ingreso.folio}.`);
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'No se pudo registrar el ingreso.');
        this.isLoading = false;
      }
    });
  }

  imprimir(): void {
    window.print();
  }
}
