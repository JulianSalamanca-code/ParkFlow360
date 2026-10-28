import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-google-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="callback-page">
      <div class="callback-panel">
        <div class="callback-spinner">
          <span class="material-symbols-outlined spinning">sync</span>
        </div>
        <h2>Completando inicio con Google</h2>
        <p>Por favor espera mientras verificamos tu cuenta...</p>
        <div *ngIf="error" class="alert alert-danger">{{ error }}</div>
      </div>
    </div>
  `,
  styles: [`
    .callback-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--pf-bg);
    }

    .callback-panel {
      text-align: center;
      padding: 48px;
      background: var(--pf-surface);
      border: 1px solid var(--pf-border);
      border-radius: var(--pf-radius-lg);
      max-width: 400px;
      width: 100%;
    }

    .callback-spinner {
      font-size: 48px;
      color: var(--pf-primary);
      margin-bottom: 24px;
    }

    .spinning {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    h2 {
      font-size: 22px;
      font-weight: 700;
      color: var(--pf-ink);
      margin-bottom: 8px;
    }

    p {
      color: var(--pf-muted);
      font-size: 15px;
    }

    .alert-danger {
      margin-top: 20px;
      padding: 12px 16px;
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.3);
      border-radius: var(--pf-radius);
      color: #ef4444;
      font-size: 14px;
    }
  `]
})
export class GoogleCallbackComponent implements OnInit {
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const error = params['error'];

      if (error) {
        this.error = 'Error en la autenticación con Google: ' + error;
        this.toast.error(this.error);
        return;
      }

      if (code) {
        this.handleCallback(code);
      } else {
        this.error = 'No se recibió código de autorización';
        this.toast.error(this.error);
      }
    });
  }

  private handleCallback(code: string): void {
    this.authService.handleGoogleCallback(code).subscribe({
      next: () => {
        const nombre = this.authService.getUser()?.nombre || '';
        this.toast.success(`¡Bienvenido${nombre ? ', ' + nombre : ''}!`);
        const rol = this.authService.getRol().toUpperCase();
        this.router.navigate([rol === 'ADMIN' ? '/vehiculos' : '/mis-parqueos']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Error al completar inicio con Google';
        this.toast.error(this.error);
      }
    });
  }
}