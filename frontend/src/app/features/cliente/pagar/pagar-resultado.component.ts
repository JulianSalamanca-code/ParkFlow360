import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-pagar-resultado',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="result-card">
        <span class="material-symbols-outlined big" [ngClass]="ok ? 'ok' : 'fail'">
          {{ ok ? 'check_circle' : 'cancel' }}
        </span>
        <h1>{{ ok ? 'Pago completado' : 'Pago no completado' }}</h1>
        <p class="text-muted">
          {{ ok
            ? 'Tu pago fue registrado y el recibo está disponible.'
            : 'El pago no se completó. Puedes intentarlo de nuevo.' }}
        </p>

        <div class="detail" *ngIf="referencia">
          <span>Referencia</span>
          <strong class="mono">{{ referencia }}</strong>
        </div>
        <div class="detail" *ngIf="estado">
          <span>Estado Wompi</span>
          <strong>{{ estado }}</strong>
        </div>

        <div class="actions">
          <a routerLink="/pagar" class="btn btn-primary">Volver a pagos</a>
          <a routerLink="/mis-parqueos" class="btn btn-secondary">Mis parqueos</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .result-card {
      max-width: 520px; margin: 40px auto; text-align: center;
      background: var(--pf-surface); border: 1px solid var(--pf-outline-soft);
      border-radius: var(--pf-radius); padding: 40px 28px;
    }
    .material-symbols-outlined.big { font-size: 64px; }
    .ok { color: var(--pf-available); }
    .fail { color: var(--pf-occupied); }
    h1 { font-size: 24px; font-weight: 800; margin: 12px 0 8px; }
    .detail {
      display: flex; justify-content: space-between;
      padding: 10px 0; border-bottom: 1px solid var(--pf-outline-soft);
      font-size: 14px; color: var(--pf-ink-soft);
    }
    .actions { display: flex; gap: 10px; justify-content: center; margin-top: 24px; flex-wrap: wrap; }
  `]
})
export class PagarResultadoComponent implements OnInit {
  ok = false;
  referencia = '';
  estado = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.referencia = params.get('reference') || params.get('referencia') || '';
    this.estado = params.get('status') || params.get('estado') || '';
    const result = (this.estado || '').toUpperCase();
    this.ok = result === 'APPROVED' || result === 'PAGADO';
  }
}
