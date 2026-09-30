import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="login-container">
      <div class="login-box">
        <h2>ParkFlow360</h2>
        <p>Inicia sesión para continuar</p>

        <form [formGroup]="form" (ngSubmit)="login()">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" class="form-control" placeholder="tu@email.com" />
            <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched" class="error">
              El email es obligatorio
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input id="password" type="password" formControlName="password" class="form-control" placeholder="••••••••" />
            <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched" class="error">
              La contraseña es obligatoria
            </div>
          </div>

          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn btn-primary" [disabled]="form.invalid || isLoading">
            {{ isLoading ? 'Cargando...' : 'Iniciar Sesión' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background-color: #f5f5f5;
    }
    .login-box {
      background: white;
      padding: 40px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      width: 100%;
      max-width: 400px;
    }
    .login-box h2 {
      text-align: center;
      color: #333;
      margin-bottom: 10px;
    }
    .login-box p {
      text-align: center;
      color: #666;
      margin-bottom: 30px;
    }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
    .form-control {
      width: 100%;
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }
    .error { color: #dc3545; font-size: 12px; margin-top: 4px; }
    .error-message {
      color: #dc3545;
      font-size: 14px;
      margin-bottom: 15px;
      text-align: center;
    }
    .btn {
      width: 100%;
      padding: 12px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
    }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-primary:disabled { background-color: #6c757d; cursor: not-allowed; }
  `]
})
export class LoginComponent {
  form!: FormGroup;
  isLoading = false;
  errorMessage = '';

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
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al iniciar sesión';
        this.isLoading = false;
      }
    });
  }
}
