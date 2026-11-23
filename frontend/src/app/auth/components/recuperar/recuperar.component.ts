import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-recuperar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="login-page">
      <div class="login-panel">
        <h1 class="login-title">Recuperar contraseña</h1>
        <p class="login-subtitle">Ingresa tu correo y te enviaremos un enlace temporal.</p>

        <form [formGroup]="form" (ngSubmit)="solicitar()">
          <div class="form-group">
            <label for="email">Correo electrónico</label>
            <input id="email" type="email" formControlName="email" class="form-control"
                   placeholder="usuario@ejemplo.com" autocomplete="email"
                   data-testid="recuperar-email" />
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <button type="submit" class="btn btn-primary btn-block"
                  [disabled]="form.invalid || isLoading" data-testid="recuperar-enviar">
            {{ isLoading ? 'Enviando...' : 'Enviar enlace' }}
          </button>
        </form>

        <div class="login-footer">
          <a routerLink="/auth/login" class="link-register">Volver al inicio de sesión</a>
        </div>
      </div>
    </div>
  `
})
export class RecuperarComponent {
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toast: ToastService
  ) {}

  solicitar(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';
    const email = String(this.form.value.email).trim().toLowerCase();

    this.authService.solicitarRecuperacion(email).subscribe({
      next: () => {
        this.isLoading = false;
        this.toast.success('Si el correo existe, recibirás un enlace de recuperación.');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'No se pudo solicitar la recuperación.';
        this.toast.error(this.errorMessage);
      }
    });
  }
}
