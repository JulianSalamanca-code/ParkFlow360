import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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

        <h1 class="login-title">Apertura de Turno</h1>
        <p class="login-subtitle">Ingresa tus credenciales de operador para iniciar la terminal.</p>

        <form [formGroup]="form" (ngSubmit)="login()">
          <div class="form-group">
            <label for="email">Usuario / Correo</label>
            <input id="email" type="email" formControlName="email" class="form-control"
                   placeholder="operador@parkflow360.com" autocomplete="username" />
            <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched" class="field-error">
              Ingresa un correo válido
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input id="password" type="password" formControlName="password" class="form-control"
                   placeholder="••••••••" autocomplete="current-password" />
            <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched" class="field-error">
              La contraseña es obligatoria
            </div>
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="form.invalid || isLoading">
            <span class="material-symbols-outlined">{{ isLoading ? 'progress_activity' : 'login' }}</span>
            {{ isLoading ? 'Validando...' : 'Iniciar Turno' }}
          </button>
        </form>

        <div class="login-foot">
          <span class="online-dot"></span>
          Terminal autorizada · ParkFlow360
        </div>
      </div>

      <div class="login-side">
        <div class="side-badge">KINETIC TERMINAL</div>
        <h2>Control de parqueaderos de alta capacidad</h2>
        <p>Gestión de vehículos, ocupación de espacios, tarifas y recaudo en una sola cabina operativa.</p>
        <div class="side-grid">
          <div class="side-cell">
            <span class="material-symbols-outlined">directions_car</span>
            Vehículos
          </div>
          <div class="side-cell">
            <span class="material-symbols-outlined">local_parking</span>
            Espacios
          </div>
          <div class="side-cell">
            <span class="material-symbols-outlined">sell</span>
            Tarifas
          </div>
          <div class="side-cell">
            <span class="material-symbols-outlined">point_of_sale</span>
            Recaudo
          </div>
        </div>
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

    .side-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 36px;
    }

    .side-cell {
      display: flex; align-items: center; gap: 10px;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: var(--pf-radius);
      padding: 14px;
      font-weight: 600; font-size: 14px;
    }

    @media (max-width: 900px) {
      .login-page { grid-template-columns: 1fr; }
      .login-side { display: none; }
    }
  `]
})
export class LoginComponent implements OnInit {
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
      password: ['', [Validators.required]]
    });
  }

  login(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading = false;
        const nombre = this.authService.getUser()?.nombre || '';
        this.toast.success(`¡Bienvenido${nombre ? ', ' + nombre : ''}!`);
        const rol = this.authService.getRol().toUpperCase();
        this.router.navigate([rol === 'ADMIN' ? '/vehiculos' : '/mis-parqueos']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al iniciar sesión';
        this.toast.error(this.errorMessage);
        this.isLoading = false;
      }
    });
  }
}
