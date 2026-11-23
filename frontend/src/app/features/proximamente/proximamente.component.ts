import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { roleLabel } from '../../core/auth/roles';

/**
 * Aterrizaje temporal para los roles que todavía no tienen módulos
 * implementados (EMPRESA, CAJERO_PAGOS, GUARDIA). Evita redirecciones
 * a rutas inexistentes hasta la Etapa 1 del plan.
 */
@Component({
  selector: 'app-proximamente',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="card" style="max-width:640px;margin:40px auto">
        <div class="card-body" style="text-align:center;padding:40px 28px">
          <span class="material-symbols-outlined" style="font-size:52px;color:var(--pf-primary)">
            construction
          </span>
          <h1 style="font-size:24px;font-weight:800;margin:14px 0 6px">
            Módulo en preparación
          </h1>
          <p class="text-muted" style="margin-bottom:6px">
            Tu rol <strong>{{ rol }}</strong> ya está reconocido por la plataforma.
          </p>
          <p class="text-muted" style="margin-bottom:24px">
            Las pantallas de empresa, cajero y guardia se habilitarán cuando el backend
            publique la migración V17 y sus endpoints.
          </p>
          <button type="button" class="btn btn-secondary" (click)="logout()"
                  [attr.data-testid]="'btn-proximamente-logout'">
            <span class="material-symbols-outlined">logout</span> Cerrar sesión
          </button>
        </div>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.Eager
})
export class ProximamenteComponent {
  constructor(private authService: AuthService, private router: Router) {}

  get rol(): string {
    return roleLabel(this.authService.getRol()) || 'Sin rol';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
