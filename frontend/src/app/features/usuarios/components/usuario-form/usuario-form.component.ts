import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { UsuarioRequest } from '../../models/usuario.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>{{ esEdicion ? 'Editar' : 'Nuevo' }} Usuario</h1>
          <p>Define las credenciales y el rol de acceso al sistema</p>
        </div>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-group">
            <label for="nombre">Nombre Completo</label>
            <input id="nombre" type="text" formControlName="nombre" class="form-control" placeholder="Juan Pérez" />
            <div *ngIf="form.get('nombre')?.invalid && form.get('nombre')?.touched" class="field-error">
              El nombre es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="email">Correo Electrónico</label>
            <input id="email" type="email" formControlName="email" class="form-control" placeholder="usuario@parkflow360.com" />
            <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched" class="field-error">
              Ingresa un correo válido
            </div>
          </div>

          <div class="form-group">
            <label for="rol">Rol</label>
            <select id="rol" formControlName="rol" class="form-control">
              <option value="">Seleccione...</option>
              <option value="ADMIN">Administrador (acceso total)</option>
              <option value="USUARIO">Usuario (reservas y sus vehículos)</option>
            </select>
            <div *ngIf="form.get('rol')?.invalid && form.get('rol')?.touched" class="field-error">
              El rol es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="password">
              Contraseña {{ esEdicion ? '(dejar vacío para no cambiar)' : '' }}
            </label>
            <input id="password" type="password" formControlName="password" class="form-control" placeholder="••••••••" />
            <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched" class="field-error">
              La contraseña debe tener al menos 6 caracteres
            </div>
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
              <span class="material-symbols-outlined">save</span> {{ isLoading ? 'Guardando...' : 'Guardar' }}
            </button>
            <a routerLink="/usuarios" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class UsuarioFormComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  usuarioId?: number;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      rol: ['', [Validators.required]],
      password: ['']
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.usuarioId = +id;
      this.cargarUsuario(this.usuarioId);
    } else {
      // En creación la contraseña es obligatoria
      this.form.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.form.get('password')?.updateValueAndValidity();
    }
  }

  cargarUsuario(id: number): void {
    this.usuarioService.obtenerPorId(id).subscribe({
      next: (usuario) => {
        this.form.patchValue({
          nombre: usuario.nombre,
          email: usuario.email,
          rol: usuario.rol
        });
      },
      error: (err) => console.error('Error al cargar usuario:', err)
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    const request: UsuarioRequest = this.form.value;
    const esEdicion = this.esEdicion && !!this.usuarioId;
    const accion = esEdicion ? 'actualizado' : 'creado';

    const operacion = esEdicion
      ? this.usuarioService.actualizar(this.usuarioId!, request)
      : this.usuarioService.crear(request);

    operacion.subscribe({
      next: () => {
        this.toast.success(`Usuario ${accion} correctamente.`);
        this.router.navigate(['/usuarios']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al guardar el usuario';
        this.toast.error(this.errorMessage);
        this.isLoading = false;
      }
    });
  }
}
