import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TarifaService } from '../../services/tarifa.service';
import { TarifaRequest } from '../../models/tarifa.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-tarifa-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ esEdicion ? 'Editar' : 'Nueva' }} Tarifa</h1>
          <p>Define el nombre, modalidad y valor de la tarifa</p>
        </div>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-group">
            <label for="nombre">Nombre</label>
            <input id="nombre" type="text" formControlName="nombre" class="form-control" placeholder="Tarifa por hora" />
            <div *ngIf="form.get('nombre')?.invalid && form.get('nombre')?.touched" class="field-error">
              El nombre es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="tipo">Tipo</label>
            <select id="tipo" formControlName="tipo" class="form-control">
              <option value="">Seleccione...</option>
              <option value="HORA">Por Hora</option>
              <option value="DIA">Por Día</option>
              <option value="SEMANA">Por Semana</option>
              <option value="MES">Mensual</option>
              <option value="MOMENTO">Por Momento</option>
            </select>
            <div *ngIf="form.get('tipo')?.invalid && form.get('tipo')?.touched" class="field-error">
              El tipo es obligatorio
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
            <label for="duracion">Duración</label>
            <input id="duracion" type="text" formControlName="duracion" class="form-control" placeholder="1 hora, 1 día, 1 mes" />
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
              <span class="material-symbols-outlined">save</span> Guardar
            </button>
            <a routerLink="/tarifas" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class TarifaFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  tarifaId?: number;

  constructor(
    private fb: FormBuilder,
    private tarifaService: TarifaService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      tipo: ['', [Validators.required, Validators.maxLength(50)]],
      valor: ['', [Validators.required, Validators.min(0)]],
      duracion: ['', [Validators.maxLength(50)]]
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.tarifaId = +id;
      this.cargarTarifa(this.tarifaId);
    }
  }

  cargarTarifa(id: number): void {
    this.tarifaService.obtenerPorId(id).subscribe({
      next: (tarifa) => {
        this.form.patchValue({
          nombre: tarifa.nombre,
          tipo: tarifa.tipo,
          valor: tarifa.valor,
          duracion: tarifa.duracion
        });
      },
      error: (err) => console.error('Error al cargar tarifa:', err)
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const request: TarifaRequest = this.form.value;
    const accion = this.esEdicion ? 'actualizada' : 'creada';

    if (this.esEdicion && this.tarifaId) {
      this.tarifaService.actualizar(this.tarifaId, request).subscribe({
        next: () => {
          this.toast.success(`Tarifa ${accion} correctamente.`);
          this.router.navigate(['/tarifas']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar la tarifa.')
      });
    } else {
      this.tarifaService.crear(request).subscribe({
        next: () => {
          this.toast.success(`Tarifa ${accion} correctamente.`);
          this.router.navigate(['/tarifas']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar la tarifa.')
      });
    }
  }
}
