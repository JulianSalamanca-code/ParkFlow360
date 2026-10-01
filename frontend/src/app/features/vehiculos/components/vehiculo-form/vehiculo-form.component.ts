import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { VehiculoService } from '../../services/vehiculo.service';
import { VehiculoRequest } from '../../models/vehiculo.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-vehiculo-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ esEdicion ? 'Editar' : 'Nuevo' }} Vehículo</h1>
          <p>Completa los datos del vehículo para registrarlo en el sistema</p>
        </div>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-group">
            <label for="placa">Placa</label>
            <input id="placa" type="text" formControlName="placa" class="form-control mono-input" placeholder="ABC123" />
            <div *ngIf="form.get('placa')?.invalid && form.get('placa')?.touched" class="field-error">
              La placa es obligatoria
            </div>
          </div>

          <div class="form-group">
            <label for="tipo">Tipo</label>
            <select id="tipo" formControlName="tipo" class="form-control">
              <option value="">Seleccione...</option>
              <option value="CARRO">Carro</option>
              <option value="MOTO">Moto</option>
              <option value="CAMIONETA">Camioneta</option>
              <option value="CAMIÓN">Camión</option>
              <option value="OTRO">Otro</option>
            </select>
            <div *ngIf="form.get('tipo')?.invalid && form.get('tipo')?.touched" class="field-error">
              El tipo es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="color">Color</label>
            <input id="color" type="text" formControlName="color" class="form-control" placeholder="Rojo" />
          </div>

          <div class="form-group">
            <label for="modelo">Modelo</label>
            <input id="modelo" type="text" formControlName="modelo" class="form-control" placeholder="Toyota Corolla" />
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
              <span class="material-symbols-outlined">save</span> Guardar
            </button>
            <a routerLink="/vehiculos" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class VehiculoFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  vehiculoId?: number;

  constructor(
    private fb: FormBuilder,
    private vehiculoService: VehiculoService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
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
    const accion = this.esEdicion ? 'actualizado' : 'creado';

    if (this.esEdicion && this.vehiculoId) {
      this.vehiculoService.actualizar(this.vehiculoId, request).subscribe({
        next: () => {
          this.toast.success(`Vehículo ${accion} correctamente.`);
          this.router.navigate(['/vehiculos']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar el vehículo.')
      });
    } else {
      this.vehiculoService.crear(request).subscribe({
        next: () => {
          this.toast.success(`Vehículo ${accion} correctamente.`);
          this.router.navigate(['/vehiculos']);
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo guardar el vehículo.')
      });
    }
  }
}
