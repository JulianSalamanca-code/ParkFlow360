import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="login-page">
      <div class="login-panel">
        <div class="login-brand">
          <span class="brand-mark">P</span>
          <div>
            <div class="brand-name">ParkFlow</div>
            <div class="brand-sub">Pro Control</div>
          </div>
        </div>

        <h1 class="login-title">Crear Cuenta</h1>
        <p class="login-subtitle">Regístrate para acceder al sistema de parqueaderos</p>

        <form [formGroup]="form" (ngSubmit)="registrar()" [attr.data-testid]="'registro-form'">
          <div class="form-group">
            <label for="email">Correo Electrónico</label>
            <input id="email" type="email" formControlName="email" class="form-control"
                   placeholder="usuario@ejemplo.com" autocomplete="email"
                   [attr.data-testid]="'input-email'" />
            <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched" class="field-error" [attr.data-testid]="'error-email'">
              <span *ngIf="form.get('email')?.errors?.['required']">El email es obligatorio</span>
              <span *ngIf="form.get('email')?.errors?.['email']">Email inválido</span>
            </div>
          </div>

          <div class="form-group">
            <label for="nombre">Nombre Completo</label>
            <input id="nombre" type="text" formControlName="nombre" class="form-control"
                   placeholder="Juan Pérez" autocomplete="name"
                   [attr.data-testid]="'input-nombre'" />
            <div *ngIf="form.get('nombre')?.invalid && form.get('nombre')?.touched" class="field-error" [attr.data-testid]="'error-nombre'">
              El nombre es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input id="password" type="password" formControlName="password" class="form-control"
                   placeholder="••••••••" autocomplete="new-password"
                   [attr.data-testid]="'input-password'" />
            <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched" class="field-error" [attr.data-testid]="'error-password'">
              <span *ngIf="form.get('password')?.errors?.['required']">La contraseña es obligatoria</span>
              <span *ngIf="form.get('password')?.errors?.['minlength']">Mínimo 6 caracteres</span>
            </div>
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirmar Contraseña</label>
            <input id="confirmPassword" type="password" formControlName="confirmPassword" class="form-control"
                   placeholder="••••••••" autocomplete="new-password"
                   [attr.data-testid]="'input-confirm-password'" />
            <div *ngIf="form.get('confirmPassword')?.invalid && form.get('confirmPassword')?.touched" class="field-error">
              Las contraseñas deben coincidir
            </div>
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger" [attr.data-testid]="'error-message'">{{ errorMessage }}</div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="form.invalid || isLoading"
                  [attr.data-testid]="'btn-registrar'">
            <span class="material-symbols-outlined">{{ isLoading ? 'progress_activity' : 'person_add' }}</span>
            {{ isLoading ? 'Registrando...' : 'Crear Cuenta' }}
          </button>
        </form>

        <div class="login-footer">
          <span>¿Ya tienes cuenta?</span>
          <a routerLink="/auth/login" class="link-register">Inicia sesión aquí</a>
        </div>

        <div class="login-foot">
          <span class="online-dot"></span>
          Terminal autorizada · ParkFlow360
        </div>
      </div>

      <div class="login-side">
        <div class="side-badge">KINETIC TERMINAL</div>
        <h2>Únete a ParkFlow360</h2>
        <p>Gestiona tus vehículos y espacios de parqueo de manera eficiente y segura.</p>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh;
      display: grid;
      grid-template-columns: 1fr 1fr;
      background: var(--pf-bg);
    }

    .login-panel {
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 48px;
      max-width: 520px;
      width: 100%;
      margin: 0 auto;
    }

    .login-brand { display: flex; align-items: center; gap: 10px; margin-bottom: 36px; }

    .login-title {
      font-size: 28px; font-weight: 800; color: var(--pf-ink);
      letter-spacing: -0.02em;
    }

    .login-subtitle { color: var(--pf-muted); font-size: 14px; margin: 8px 0 28px; }

    .btn-block { width: 100%; padding: 13px; font-size: 15px; margin-top: 6px; }

    .login-foot {
      display: flex; align-items: center; gap: 8px;
      margin-top: 28px; color: var(--pf-muted); font-size: 12px;
    }

    .login-footer {
      display: flex; justify-content: center; align-items: center; gap: 8px;
      margin-top: 20px; color: var(--pf-muted); font-size: 14px;
    }

    .link-register {
      color: var(--pf-primary); text-decoration: none; font-weight: 600;
    }
    .link-register:hover { text-decoration: underline; }

    .login-side {
      background:
        linear-gradient(135deg, rgba(30, 58, 138, 0.94), rgba(15, 23, 42, 0.96)),
        repeating-linear-gradient(45deg, #1e3a8a 0 12px, #1b3479 12px 24px);
      color: #fff;
      padding: 56px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .side-badge {
      display: inline-block; align-self: flex-start;
      font-family: var(--pf-mono); font-size: 11px; font-weight: 700;
      letter-spacing: 0.16em; text-transform: uppercase;
      background: rgba(255, 255, 255, 0.12);
      border: 1px solid rgba(255, 255, 255, 0.2);
      padding: 5px 12px; border-radius: 999px; margin-bottom: 22px;
    }

    .login-side h2 { font-size: 30px; font-weight: 800; line-height: 1.2; letter-spacing: -0.02em; }
    .login-side p { margin-top: 14px; color: rgba(255, 255, 255, 0.75); font-size: 15px; max-width: 420px; }

    @media (max-width: 900px) {
      .login-page { grid-template-columns: 1fr; }
      .login-side { display: none; }
    }
  `]
})
export class RegistroComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      nombre: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  registrar(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    const { confirmPassword, ...registroData } = this.form.value;

    this.authService.registrar(registroData).subscribe({
      next: () => {
        this.isLoading = false;
        this.toast.success('¡Cuenta creada exitosamente!');
        this.router.navigate(['/mis-parqueos']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al crear la cuenta';
        this.toast.error(this.errorMessage);
        this.isLoading = false;
      }
    });
  }
}