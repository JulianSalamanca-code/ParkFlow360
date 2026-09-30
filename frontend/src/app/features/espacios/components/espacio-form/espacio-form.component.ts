import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EspacioService } from '../../services/espacio.service';
import { EspacioRequest } from '../../models/espacio.model';

@Component({
  selector: 'app-espacio-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="espacio-form">
      <h2>{{ esEdicion ? 'Editar' : 'Nuevo' }} Espacio</h2>

      <form [formGroup]="form" (ngSubmit)="guardar()">
        <div class="form-group">
          <label for="numero">Número *</label>
          <input id="numero" type="text" formControlName="numero" class="form-control" />
          <div *ngIf="form.get('numero')?.invalid && form.get('numero')?.touched" class="error">
            El número es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="tipo">Tipo *</label>
          <select id="tipo" formControlName="tipo" class="form-control">
            <option value="">Seleccione...</option>
            <option value="CARRO">Carro</option>
            <option value="MOTO">Moto</option>
            <option value="CAMIONETA">Camioneta</option>
            <option value="CAMIÓN">Camión</option>
            <option value="DISCAPACITADO">Discapacitado</option>
            <option value="ELECTRICO">Eléctrico</option>
          </select>
          <div *ngIf="form.get('tipo')?.invalid && form.get('tipo')?.touched" class="error">
            El tipo es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="estado">Estado *</label>
          <select id="estado" formControlName="estado" class="form-control">
            <option value="">Seleccione...</option>
            <option value="LIBRE">Libre</option>
            <option value="OCUPADO">Ocupado</option>
            <option value="RESERVADO">Reservado</option>
            <option value="MANTENIMIENTO">Mantenimiento</option>
          </select>
          <div *ngIf="form.get('estado')?.invalid && form.get('estado')?.touched" class="error">
            El estado es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="piso">Piso *</label>
          <input id="piso" type="number" formControlName="piso" class="form-control" />
          <div *ngIf="form.get('piso')?.invalid && form.get('piso')?.touched" class="error">
            El piso es obligatorio
          </div>
        </div>

        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Guardar</button>
          <a routerLink="/espacios" class="btn btn-secondary">Cancelar</a>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .espacio-form { padding: 20px; max-width: 500px; }
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
export class EspacioFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  espacioId?: number;

  constructor(
    private fb: FormBuilder,
    private espacioService: EspacioService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      numero: ['', [Validators.required, Validators.maxLength(20)]],
      tipo: ['', [Validators.required, Validators.maxLength(50)]],
      estado: ['', [Validators.required, Validators.maxLength(20)]],
      piso: ['', [Validators.required]]
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.espacioId = +id;
      this.cargarEspacio(this.espacioId);
    }
  }

  cargarEspacio(id: number): void {
    this.espacioService.obtenerPorId(id).subscribe({
      next: (espacio) => {
        this.form.patchValue({
          numero: espacio.numero,
          tipo: espacio.tipo,
          estado: espacio.estado,
          piso: espacio.piso
        });
      },
      error: (err) => console.error('Error al cargar espacio:', err)
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const request: EspacioRequest = this.form.value;

    if (this.esEdicion && this.espacioId) {
      this.espacioService.actualizar(this.espacioId, request).subscribe({
        next: () => this.router.navigate(['/espacios']),
        error: (err) => console.error('Error al actualizar:', err)
      });
    } else {
      this.espacioService.crear(request).subscribe({
        next: () => this.router.navigate(['/espacios']),
        error: (err) => console.error('Error al crear:', err)
      });
    }
  }
}
