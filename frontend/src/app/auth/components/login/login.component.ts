import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-page">
      <div class="login-card">
        <div class="brand">
          <span class="brand-mark">P</span>
          <span class="brand-text">ParkFlow<span>360</span></span>
        </div>

        <h1 class="login-title">Bienvenido de nuevo</h1>
        <p class="login-subtitle">Gestiona tus parqueaderos de forma inteligente</p>

        <form [formGroup]="form" (ngSubmit)="login()">
          <div class="form-group">
            <label for="email">Correo electrónico</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              class="form-control"
              placeholder="tu@empresa.com"
              autocomplete="username" />
            <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched" class="field-error">
              Ingresa un correo válido
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              class="form-control"
              placeholder="••••••••"
              autocomplete="current-password" />
            <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched" class="field-error">
              La contraseña es obligatoria
            </div>
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger">
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="form.invalid || isLoading">
            <span *ngIf="!isLoading">Iniciar Sesión</span>
            <span *ngIf="isLoading">Ingresando...</span>
          </button>
        </form>

        <p class="login-footer">ParkFlow360 © {{ year }} · Gestión de parqueaderos</p>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background:
        radial-gradient(circle at 15% 20%, rgba(41, 182, 246, 0.18), transparent 45%),
        radial-gradient(circle at 85% 80%, rgba(79, 195, 247, 0.14), transparent 45%),
        linear-gradient(135deg, #0b1120, #16213e 60%, #1a2745);
    }

    .login-card {
      width: 100%;
      max-width: 420px;
      background: #ffffff;
      border-radius: 16px;
      padding: 40px 36px 28px;
      box-shadow: 0 24px 60px rgba(11, 17, 32, 0.45);
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 28px;
    }

    .brand-mark {
      width: 40px;
      height: 40px;
      border-radius: 11px;
      background: linear-gradient(135deg, #29b6f6, #4fc3f7);
      color: #0b1120;
      font-weight: 900;
      font-size: 22px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 6px 16px rgba(41, 182, 246, 0.45);
    }

    .brand-text {
      font-size: 22px;
      font-weight: 800;
      color: #16213e;
      letter-spacing: -0.02em;
    }

    .brand-text span {
      color: #29b6f6;
    }

    .login-title {
      font-size: 24px;
      font-weight: 800;
      color: #16213e;
      letter-spacing: -0.02em;
    }

    .login-subtitle {
      color: #64748b;
      font-size: 14px;
      margin: 6px 0 28px;
    }

    .btn-block {
      width: 100%;
      padding: 13px;
      margin-top: 8px;
      font-size: 15px;
    }

    .login-footer {
      text-align: center;
      color: #94a3b8;
      font-size: 12px;
      margin-top: 26px;
    }
  `]
})
export class LoginComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  errorMessage = '';
  year = new Date().getFullYear();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
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
        this.router.navigate(['/vehiculos']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al iniciar sesión';
        this.isLoading = false;
      }
    });
  }
}
