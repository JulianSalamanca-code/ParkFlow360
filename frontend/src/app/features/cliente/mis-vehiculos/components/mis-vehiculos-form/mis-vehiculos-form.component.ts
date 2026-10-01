import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { VehiculoService } from '../../../../vehiculos/services/vehiculo.service';
import { VehiculoRequest } from '../../../../vehiculos/models/vehiculo.model';
import { ToastService } from '../../../../../shared/services/toast.service';

@Component({
  selector: 'app-mis-vehiculos-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Agregar Vehículo</h1>
          <p>Registra un vehículo a tu nombre</p>
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

          <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
              <span class="material-symbols-outlined">save</span> Guardar
            </button>
            <a routerLink="/mis-vehiculos" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class MisVehiculosFormComponent implements OnInit {
  form!: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private vehiculoService: VehiculoService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      placa: ['', [Validators.required, Validators.maxLength(20)]],
      tipo: ['', [Validators.required, Validators.maxLength(50)]],
      color: ['', [Validators.maxLength(50)]],
      modelo: ['', [Validators.maxLength(100)]]
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const request: VehiculoRequest = this.form.value;

    this.vehiculoService.crearMio(request).subscribe({
      next: () => {
        this.toast.success('Vehículo agregado correctamente.');
        this.router.navigate(['/mis-vehiculos']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al guardar el vehículo';
        this.toast.error(this.errorMessage);
      }
    });
  }
}
