import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { VehiculoService } from '../../services/vehiculo.service';
import { VehiculoRequest } from '../../models/vehiculo.model';

@Component({
  selector: 'app-vehiculo-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="vehiculo-form">
      <h2>{{ esEdicion ? 'Editar' : 'Nuevo' }} Vehículo</h2>

      <form [formGroup]="form" (ngSubmit)="guardar()">
        <div class="form-group">
          <label for="placa">Placa *</label>
          <input id="placa" type="text" formControlName="placa" class="form-control" />
          <div *ngIf="form.get('placa')?.invalid && form.get('placa')?.touched" class="error">
            La placa es obligatoria
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
            <option value="OTRO">Otro</option>
          </select>
          <div *ngIf="form.get('tipo')?.invalid && form.get('tipo')?.touched" class="error">
            El tipo es obligatorio
          </div>
        </div>

        <div class="form-group">
          <label for="color">Color</label>
          <input id="color" type="text" formControlName="color" class="form-control" />
        </div>

        <div class="form-group">
          <label for="modelo">Modelo</label>
          <input id="modelo" type="text" formControlName="modelo" class="form-control" />
        </div>

        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Guardar</button>
          <a routerLink="/vehiculos" class="btn btn-secondary">Cancelar</a>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .vehiculo-form { padding: 20px; max-width: 500px; }
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
export class VehiculoFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  vehiculoId?: number;

  constructor(
    private fb: FormBuilder,
    private vehiculoService: VehiculoService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      placa: ['', [Validators.required, Validators.maxLength(20)]],
      tipo: ['', [Validators.required, Validators.maxLength(50)]],
      color: ['', [Validators.maxLength(50)]],
      modelo: ['', [Validators.maxLength(100)]]
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.vehiculoId = +id;
      this.cargarVehiculo(this.vehiculoId);
    }
  }

  cargarVehiculo(id: number): void {
    this.vehiculoService.obtenerPorId(id).subscribe({
      next: (vehiculo) => {
        this.form.patchValue({
          placa: vehiculo.placa,
          tipo: vehiculo.tipo,
          color: vehiculo.color,
          modelo: vehiculo.modelo
        });
      },
      error: (err) => console.error('Error al cargar vehículo:', err)
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const request: VehiculoRequest = this.form.value;

    if (this.esEdicion && this.vehiculoId) {
      this.vehiculoService.actualizar(this.vehiculoId, request).subscribe({
        next: () => this.router.navigate(['/vehiculos']),
        error: (err) => console.error('Error al actualizar:', err)
      });
    } else {
      this.vehiculoService.crear(request).subscribe({
        next: () => this.router.navigate(['/vehiculos']),
        error: (err) => console.error('Error al crear:', err)
      });
    }
  }
}
