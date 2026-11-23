import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-restablecer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="login-page">
      <div class="login-panel">
        <h1 class="login-title">Crear nueva contraseña</h1>
        <p class="login-subtitle">Ingresa y confirma tu nueva contraseña.</p>

        <form [formGroup]="form" (ngSubmit)="restablecer()">
          <div class="form-group">
            <label for="password">Nueva contraseña</label>
            <input id="password" type="password" formControlName="password"
                   class="form-control" autocomplete="new-password"
                   data-testid="restablecer-password" />
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirmar contraseña</label>
            <input id="confirmPassword" type="password" formControlName="confirmPassword"
                   class="form-control" autocomplete="new-password"
                   data-testid="restablecer-confirmar" />
          </div>

          <div *ngIf="form.errors?.['passwordMismatch'] && form.touched" class="field-error">
            Las contraseñas deben coincidir
          </div>
          <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <button type="submit" class="btn btn-primary btn-block"
                  [disabled]="form.invalid || isLoading" data-testid="restablecer-enviar">
            {{ isLoading ? 'Guardando...' : 'Restablecer contraseña' }}
          </button>
        </form>

        <div class="login-footer">
          <a routerLink="/auth/login" class="link-register">Volver al inicio de sesión</a>
        </div>
      </div>
    </div>
  `
})
export class RestablecerComponent implements OnInit {
  form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });
  isLoading = false;
  errorMessage = '';
  private token = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') || '';
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  restablecer(): void {
    if (this.form.invalid || !this.token) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.authService.restablecerPassword(this.token, String(this.form.value.password)).subscribe({
      next: () => {
        this.isLoading = false;
        this.toast.success('Contraseña restablecida correctamente.');
        this.router.navigateByUrl('/auth/login');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'No se pudo restablecer la contraseña.';
        this.toast.error(this.errorMessage);
      }
    });
  }
}
