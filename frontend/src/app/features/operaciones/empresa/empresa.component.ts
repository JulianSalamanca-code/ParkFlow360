import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OperacionesService } from '../services/operaciones.service';
import {
  ConfiguracionEmpresa,
  EmpresaResumen,
  ParqueaderoOperativo,
  ReporteOperativo
} from '../models/operaciones.model';

type EmpresaView = 'resumen' | 'parqueaderos' | 'reportes' | 'configuracion';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>{{ pageTitle }}</h1>
          <p>Administración de {{ resumen?.empresa || 'tu empresa' }}</p>
        </div>
        <span class="badge badge-available"><span class="online-dot"></span> Operación activa</span>
      </header>

      <div *ngIf="loading" class="card state" data-testid="empresa-loading" role="status">
        <span class="material-symbols-outlined">progress_activity</span> Cargando información...
      </div>

      <div *ngIf="successMessage" class="alert alert-success" data-testid="empresa-success" role="status">{{ successMessage }}</div>
      <div *ngIf="!loading && errorMessage" class="alert alert-danger" data-testid="empresa-error" role="alert">
        <span>{{ errorMessage }}</span>
        <button class="btn btn-secondary btn-sm" type="button" (click)="load()">Reintentar</button>
      </div>

      <ng-container *ngIf="!loading && !errorMessage">
        <ng-container [ngSwitch]="view">
          <ng-container *ngSwitchCase="'resumen'">
            <div *ngIf="resumen && resumen.parqueaderosActivos > 0; else empresaEmpty">
              <div class="metrics" data-testid="empresa-metricas">
                <article class="metric"><div class="label">Parqueaderos activos</div><div class="value">{{ resumen.parqueaderosActivos }}</div></article>
                <article class="metric metric-occupied"><div class="label">Espacios ocupados</div><div class="value">{{ resumen.espaciosOcupados }} / {{ resumen.espaciosTotales }}</div></article>
                <article class="metric metric-available"><div class="label">Ingresos hoy</div><div class="value">{{ resumen.ingresosHoy }}</div></article>
                <article class="metric metric-warning"><div class="label">Recaudo del día</div><div class="value">{{ resumen.recaudoHoy | currency:'COP':'symbol-narrow':'1.0-0' }}</div></article>
              </div>
              <div class="card card-body overview">
                <div>
                  <h2>Tu operación, en un solo lugar</h2>
                  <p>Consulta sedes, rendimiento y datos generales de la empresa.</p>
                </div>
                <a class="btn btn-primary" routerLink="/empresa/parqueaderos" data-testid="empresa-ver-parqueaderos">Ver parqueaderos</a>
              </div>
            </div>
          </ng-container>

          <ng-container *ngSwitchCase="'parqueaderos'">
            <div class="card table-wrap" *ngIf="parqueaderos.length; else empresaEmpty">
              <table class="table" data-testid="empresa-parqueaderos">
                <thead><tr><th>Parqueadero</th><th>Identificador</th><th>Dirección</th><th>Ocupación</th><th>Estado</th></tr></thead>
                <tbody>
                  <tr *ngFor="let parqueadero of parqueaderos">
                    <td>{{ parqueadero.nombre }}</td>
                    <td class="mono">{{ parqueadero.identificador }}</td>
                    <td>{{ parqueadero.direccion }}</td>
                    <td>{{ parqueadero.ocupados }} / {{ parqueadero.capacidad }}</td>
                    <td><span class="badge" [class.badge-available]="parqueadero.activo" [class.badge-neutral]="!parqueadero.activo">{{ parqueadero.activo ? 'Activo' : 'Inactivo' }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </ng-container>

          <ng-container *ngSwitchCase="'reportes'">
            <div class="card table-wrap" *ngIf="reportes.length; else empresaEmpty">
              <table class="table" data-testid="empresa-reportes">
                <thead><tr><th>Periodo</th><th>Ingresos</th><th>Recaudo</th><th>Ocupación promedio</th></tr></thead>
                <tbody>
                  <tr *ngFor="let reporte of reportes">
                    <td>{{ reporte.periodo }}</td><td>{{ reporte.ingresos }}</td>
                    <td>{{ reporte.recaudo | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
                    <td>{{ reporte.ocupacionPromedio }}%</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </ng-container>

          <ng-container *ngSwitchCase="'configuracion'">
            <article class="card card-body settings" *ngIf="configuracion?.razonSocial; else empresaEmpty" data-testid="empresa-configuracion">
              <div><span class="label">Razón social</span><strong>{{ configuracion?.razonSocial }}</strong></div>
              <div><span class="label">Correo de contacto</span><strong>{{ configuracion?.correoContacto }}</strong></div>
              <div><span class="label">Zona horaria</span><strong>{{ configuracion?.zonaHoraria }}</strong></div>
              <form class="tolerancia-form" (ngSubmit)="guardarConfiguracion()">
                <label for="tolerancia-salida">Tolerancia de salida (minutos)</label>
                <div class="tolerancia-controls">
                  <input id="tolerancia-salida" class="form-control" type="number" min="0" max="240" step="1"
                         name="toleranciaSalidaMinutos" [(ngModel)]="toleranciaInput" required
                         data-testid="empresa-tolerancia" />
                  <button class="btn btn-primary" type="submit"
                          [disabled]="savingConfiguracion || configuracion?.empresaId == null
                            || !esToleranciaValida || toleranciaInput === configuracion?.toleranciaSalidaMinutos"
                          data-testid="empresa-guardar-tolerancia">
                    {{ savingConfiguracion ? 'Guardando...' : 'Guardar tolerancia' }}
                  </button>
                </div>
                <small class="text-muted">Aplica a todos los parqueaderos de la empresa. El contador se reinicia cuando el cajero confirma el pago.</small>
              </form>
            </article>
          </ng-container>
        </ng-container>
      </ng-container>

      <ng-template #empresaEmpty>
        <div class="card state empty" data-testid="empresa-empty">
          <span class="material-symbols-outlined">inbox</span>
          <h2>Sin información para mostrar</h2>
          <p>Cuando haya datos disponibles para esta sección, aparecerán aquí.</p>
        </div>
      </ng-template>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [`
    .page { max-width: 1180px; margin: 0 auto; }
    .badge-available { display: inline-flex; align-items: center; gap: 7px; }
    .badge-available .online-dot { width: 7px; height: 7px; }
    .overview { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
    .overview h2 { font-size: 17px; margin-bottom: 5px; }
    .overview p, .settings p { color: var(--pf-muted); }
    .state { min-height: 180px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; text-align: center; padding: 28px; }
    .state .material-symbols-outlined { font-size: 30px; color: var(--pf-muted); }
    .state h2 { font-size: 17px; }
    .state p { color: var(--pf-muted); }
    .empty { border-style: dashed; }
    .alert { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .alert-success { background: var(--pf-available-bg); border-color: var(--pf-available-border); color: var(--pf-available-text); }
    .tolerancia-form { display: grid; gap: 8px; }
    .tolerancia-form label { font-size: 13px; font-weight: 600; }
    .tolerancia-controls { display: flex; align-items: center; gap: 10px; }
    .tolerancia-controls .form-control { max-width: 130px; }
    .settings { max-width: 720px; display: grid; gap: 20px; }
    .settings div { display: grid; gap: 5px; }
    .settings .label { color: var(--pf-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
    @media (max-width: 600px) { .overview { align-items: flex-start; flex-direction: column; } }
  `]
})
export class EmpresaComponent {
  readonly view: EmpresaView;
  loading = true;
  errorMessage = '';
  successMessage = '';
  resumen: EmpresaResumen | null = null;
  parqueaderos: ParqueaderoOperativo[] = [];
  reportes: ReporteOperativo[] = [];
  configuracion: ConfiguracionEmpresa | null = null;
  toleranciaInput = 30;
  savingConfiguracion = false;

  constructor(route: ActivatedRoute, private operaciones: OperacionesService) {
    this.view = route.snapshot.data['view'] as EmpresaView;
    this.load();
  }

  get esToleranciaValida(): boolean {
    return Number.isInteger(this.toleranciaInput)
      && this.toleranciaInput >= 0
      && this.toleranciaInput <= 240;
  }

  get pageTitle(): string {
    const titles: Readonly<Record<EmpresaView, string>> = {
      resumen: 'Resumen de la empresa',
      parqueaderos: 'Parqueaderos y operación',
      reportes: 'Reportes operativos',
      configuracion: 'Configuración básica'
    };
    return titles[this.view];
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    const onError = (): void => {
      this.loading = false;
      this.errorMessage = 'No se pudo cargar la información para este usuario o parqueadero.';
    };

    switch (this.view) {
      case 'resumen':
        this.operaciones.getEmpresaResumen().subscribe({
          next: value => { this.resumen = value; this.loading = false; },
          error: onError
        });
        break;
      case 'parqueaderos':
        this.operaciones.getParqueaderos().subscribe({
          next: value => { this.parqueaderos = value; this.loading = false; },
          error: onError
        });
        break;
      case 'reportes':
        this.operaciones.getReportes().subscribe({
          next: value => { this.reportes = value; this.loading = false; },
          error: onError
        });
        break;
      case 'configuracion':
        this.operaciones.getConfiguracion().subscribe({
          next: value => {
            this.configuracion = value;
            this.toleranciaInput = value.toleranciaSalidaMinutos;
            this.loading = false;
          },
          error: onError
        });
        break;
    }
  }

  guardarConfiguracion(): void {
    const empresaId = this.configuracion?.empresaId;
    if (empresaId == null || !this.esToleranciaValida) {
      this.errorMessage = 'Ingresa una tolerancia entre 0 y 240 minutos.';
      return;
    }

    this.savingConfiguracion = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.operaciones.guardarConfiguracion(empresaId, this.toleranciaInput).subscribe({
      next: value => {
        this.configuracion = value;
        this.toleranciaInput = value.toleranciaSalidaMinutos;
        this.savingConfiguracion = false;
        this.successMessage = 'Tolerancia de salida actualizada para todos los parqueaderos.';
      },
      error: () => {
        this.savingConfiguracion = false;
        this.errorMessage = 'No se pudo guardar la tolerancia de salida. Intenta nuevamente.';
      }
    });
  }
}
