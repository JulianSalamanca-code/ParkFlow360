import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TarifaService } from '../../services/tarifa.service';
import { TarifaRequest } from '../../models/tarifa.model';

@Component({
  selector: 'app-tarifa-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="tarifa-form">
      <h2>{{ esEdicion ? 'Editar' : 'Nueva' }} Tarifa</h2>

      <form [formGroup]="form" (ngSubmit)="guardar()">
        <div class="form-group">
          <label for="nombre">Nombre *</label>
          <input id="nombre" type="text" formControlName="nombre" class="form-control" />
          <div *ngIf="form.get('nombre')?.invalid && form.get('nombre')?.touched" class="error">
            El nombre es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="tipo">Tipo *</label>
          <select id="tipo" formControlName="tipo" class="form-control">
            <option value="">Seleccione...</option>
            <option value="HORA">Por Hora</option>
            <option value="DIA">Por Día</option>
            <option value="SEMANA">Por Semana</option>
            <option value="MES">Mensual</option>
            <option value="MOMENTO">Por Momento</option>
          </select>
          <div *ngIf="form.get('tipo')?.invalid && form.get('tipo')?.touched" class="error">
            El tipo es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="valor">Valor *</label>
          <input id="valor" type="number" formControlName="valor" class="form-control" step="0.01" min="0" />
          <div *ngIf="form.get('valor')?.invalid && form.get('valor')?.touched" class="error">
            El valor es obligatorio y debe ser positivo
          </div>
        </div>

        <div class="form-group">
          <label for="duracion">Duración</label>
          <input id="duracion" type="text" formControlName="duracion" class="form-control" placeholder="Ej: 1 hora, 1 día, 1 mes" />
        </div>

        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Guardar</button>
          <a routerLink="/tarifas" class="btn btn-secondary">Cancelar</a>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .tarifa-form { padding: 20px; max-width: 500px; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
    .form-control { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
    .error { color: #dc3545; font-size: 12px; margin-top: 4px; }
    .actions { margin-top: 20px; }
    .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; margin-right: 8px; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-secondary { background-color: #6c757d; color: white; }
  `]
})
export class TarifaFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  tarifaId?: number;

  constructor(
    private fb: FormBuilder,
    private tarifaService: TarifaService,
    private router: Router,
    private route: ActivatedRoute
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

    if (this.esEdicion && this.tarifaId) {
      this.tarifaService.actualizar(this.tarifaId, request).subscribe({
        next: () => this.router.navigate(['/tarifas']),
        error: (err) => console.error('Error al actualizar:', err)
      });
    } else {
      this.tarifaService.crear(request).subscribe({
        next: () => this.router.navigate(['/tarifas']),
        error: (err) => console.error('Error al crear:', err)
      });
    }
  }
}
