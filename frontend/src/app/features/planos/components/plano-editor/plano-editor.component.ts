import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlanoService } from '../../services/plano.service';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-plano-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ id ? 'Editar Plano' : 'Nuevo Plano' }}</h1>
          <p>Sube la imagen del plano, define la cuadrícula y genera los espacios en bloque</p>
        </div>
        <a routerLink="/planos" class="btn btn-secondary">
          <span class="material-symbols-outlined">arrow_back</span> Volver
        </a>
      </div>

      <div class="editor-grid">
        <div class="form-card" style="max-width:none">
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <div class="form-header"><h2>Datos del plano</h2></div>

            <div class="form-group">
              <label for="nombre">Nombre *</label>
              <input id="nombre" formControlName="nombre" class="form-control" placeholder="Plano nivel 1" />
            </div>

            <div class="form-group">
              <label for="descripcion">Descripción</label>
              <input id="descripcion" formControlName="descripcion" class="form-control"
                     placeholder="Parqueadero principal" />
            </div>

            <div class="form-group">
              <label for="imagenUrl">Imagen del plano (URL)</label>
              <input id="imagenUrl" formControlName="imagenUrl" class="form-control"
                     placeholder="https://.../plano.png" />
            </div>

            <div class="row-3">
              <div class="form-group">
                <label for="piso">Piso</label>
                <input id="piso" type="number" formControlName="piso" class="form-control" />
              </div>
              <div class="form-group">
                <label for="filas">Filas *</label>
                <input id="filas" type="number" min="1" formControlName="filas" class="form-control" />
              </div>
              <div class="form-group">
                <label for="columnas">Columnas *</label>
                <input id="columnas" type="number" min="1" formControlName="columnas" class="form-control" />
              </div>
            </div>

            <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
                <span class="material-symbols-outlined">save</span>
                {{ isLoading ? 'Guardando...' : (id ? 'Guardar cambios' : 'Crear plano') }}
              </button>
            </div>
          </form>

          <div *ngIf="id" class="generate-block">
            <div class="form-header"><h2>Generar espacios en bloque</h2></div>
            <p class="text-muted" style="margin-bottom:14px">
              A partir de la cuadrícula se crean automáticamente todos los espacios; no hay que registrarlos uno por uno.
            </p>

            <form [formGroup]="genForm" (ngSubmit)="generar()">
              <div class="row-3">
                <div class="form-group">
                  <label for="prefijo">Prefijo</label>
                  <input id="prefijo" formControlName="prefijo" class="form-control" placeholder="A" />
                </div>
                <div class="form-group">
                  <label for="tipo">Tipo *</label>
                  <select id="tipo" formControlName="tipo" class="form-control">
                    <option value="CARRO">CARRO</option>
                    <option value="MOTO">MOTO</option>
                    <option value="CAMIONETA">CAMIONETA</option>
                    <option value="BICICLETA">BICICLETA</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="pisoGen">Piso</label>
                  <input id="pisoGen" type="number" formControlName="piso" class="form-control" />
                </div>
              </div>
              <div class="row-3">
                <div class="form-group">
                  <label for="filasGen">Filas</label>
                  <input id="filasGen" type="number" min="1" formControlName="filas" class="form-control" />
                </div>
                <div class="form-group">
                  <label for="columnasGen">Columnas</label>
                  <input id="columnasGen" type="number" min="1" formControlName="columnas" class="form-control" />
                </div>
                <div class="form-group">
                  <label for="estadoInicial">Estado inicial</label>
                  <select id="estadoInicial" formControlName="estadoInicial" class="form-control">
                    <option value="LIBRE">LIBRE</option>
                    <option value="MANTENIMIENTO">MANTENIMIENTO</option>
                  </select>
                </div>
              </div>

              <div *ngIf="genMessage" class="alert" [ngClass]="genOk ? 'alert-ok' : 'alert-danger'">{{ genMessage }}</div>

              <div class="form-actions">
                <button type="submit" class="btn btn-secondary" [disabled]="genForm.invalid || isGenerating">
                  <span class="material-symbols-outlined">grid_on</span>
                  {{ isGenerating ? 'Generando...' : 'Generar espacios' }}
                </button>
              </div>
            </form>
          </div>
        </div>

        <div class="preview">
          <div class="ticket-head">
            <span class="material-symbols-outlined">map</span>
            <strong>Vista previa de la cuadrícula</strong>
          </div>
          <div class="plano-canvas" [style.background-image]="imagenUrl ? 'url(' + imagenUrl + ')' : null">
            <div class="grid" [style.grid-template-columns]="'repeat(' + cols + ', 1fr)'">
              <div class="cell" *ngFor="let c of celdas"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .editor-grid { display: grid; grid-template-columns: 1fr 420px; gap: 24px; align-items: start; }
    .row-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
    .generate-block { margin-top: 28px; padding-top: 20px; border-top: 1px solid var(--pf-outline-soft); }
    .preview { position: sticky; top: 80px; }
    .ticket-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
    .ticket-head .material-symbols-outlined { color: var(--pf-primary); }
    .plano-canvas {
      background: var(--pf-surface-alt);
      border: 1px solid var(--pf-outline);
      border-radius: var(--pf-radius);
      padding: 16px;
      background-size: contain;
      background-repeat: no-repeat;
      background-position: center;
    }
    .grid { display: grid; gap: 6px; }
    .cell { aspect-ratio: 3 / 2; background: rgba(30, 58, 138, 0.12); border: 1px dashed var(--pf-primary); border-radius: 4px; }
    .alert-ok { background: var(--pf-available-bg); border-color: var(--pf-available-border); color: var(--pf-available-text); }
    @media (max-width: 1000px) {
      .editor-grid { grid-template-columns: 1fr; }
      .preview { position: static; }
    }
    @media (max-width: 560px) {
      .row-3 { grid-template-columns: 1fr; }
    }
  `]
})
export class PlanoEditorComponent implements OnInit {
  form!: FormGroup;
  genForm!: FormGroup;
  id?: number;
  isLoading = false;
  isGenerating = false;
  errorMessage = '';
  genMessage = '';
  genOk = false;

  constructor(
    private fb: FormBuilder,
    private planoService: PlanoService,
    private route: ActivatedRoute,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required]],
      descripcion: [''],
      imagenUrl: [''],
      piso: [1],
      filas: [3, [Validators.required, Validators.min(1)]],
      columnas: [5, [Validators.required, Validators.min(1)]]
    });

    this.genForm = this.fb.group({
      prefijo: [''],
      tipo: ['CARRO', [Validators.required]],
      piso: [1],
      filas: [3, [Validators.min(1)]],
      columnas: [5, [Validators.min(1)]],
      estadoInicial: ['LIBRE']
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.cargar(this.id);
    }
  }

  cargar(id: number): void {
    this.planoService.obtenerPorId(id).subscribe({
      next: (p) => {
        this.form.patchValue({
          nombre: p.nombre,
          descripcion: p.descripcion,
          imagenUrl: p.imagenUrl,
          piso: p.piso,
          filas: p.filas || 3,
          columnas: p.columnas || 5
        });
        this.genForm.patchValue({ piso: p.piso, filas: p.filas || 3, columnas: p.columnas || 5 });
      },
      error: () => this.toast.error('No se pudo cargar el plano.')
    });
  }

  get imagenUrl(): string {
    return this.form?.get('imagenUrl')?.value || '';
  }

  get cols(): number {
    return Math.max(1, Number(this.form?.get('columnas')?.value) || 1);
  }

  get celdas(): number[] {
    const rows = Math.max(1, Number(this.form?.get('filas')?.value) || 1);
    return Array.from({ length: rows * this.cols });
  }

  guardar(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.errorMessage = '';

    const payload = this.form.value;
    const op = this.id
      ? this.planoService.actualizar(this.id, payload)
      : this.planoService.crear(payload);

    op.subscribe({
      next: (plano) => {
        this.isLoading = false;
        this.toast.success(this.id ? 'Plano actualizado.' : 'Plano creado.');
        this.id = plano.id;
        if (!this.form.get('imagenUrl')?.value) {
          this.router.navigate(['/planos']);
        } else {
          this.router.navigate(['/planos']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'No se pudo guardar el plano.';
      }
    });
  }

  generar(): void {
    if (!this.id || this.genForm.invalid) return;
    this.isGenerating = true;
    this.genMessage = '';

    this.planoService.generarEspacios(this.id, this.genForm.value).subscribe({
      next: (espacios) => {
        this.isGenerating = false;
        this.genOk = true;
        this.genMessage = `Se generaron ${espacios.length} espacios nuevos.`;
        this.toast.success(this.genMessage);
      },
      error: (err) => {
        this.isGenerating = false;
        this.genOk = false;
        this.genMessage = err.error?.message || 'No se pudieron generar los espacios.';
      }
    });
  }
}
