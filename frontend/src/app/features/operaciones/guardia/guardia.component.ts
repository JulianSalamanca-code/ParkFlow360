import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { OperacionesService } from '../services/operaciones.service';
import {
  AccionIngreso,
  IngresoOperativo,
  NovedadResponse,
  OcupacionOperativa
} from '../models/operaciones.model';

type GuardiaView = 'ingresos' | 'ocupacion';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>{{ view === 'ingresos' ? 'Control de accesos' : 'Estado del parqueadero' }}</h1>
          <p>{{ view === 'ingresos' ? 'Valida entradas y salidas del turno.' : 'Consulta la ocupación actual por estado.' }}</p>
        </div>
        <span class="badge badge-neutral" *ngIf="view === 'ingresos'">{{ ingresos.length }} activos</span>
      </header>

      <div *ngIf="successMessage" class="alert alert-success" data-testid="guardia-success" role="status">{{ successMessage }}</div>
      <div *ngIf="loading" class="card state" data-testid="guardia-loading" role="status">
        <span class="material-symbols-outlined">progress_activity</span> Cargando operación...
      </div>
      <div *ngIf="!loading && errorMessage" class="alert alert-danger" data-testid="guardia-error" role="alert">
        <span>{{ errorMessage }}</span>
        <button class="btn btn-secondary btn-sm" type="button" (click)="load()">Reintentar</button>
      </div>

      <ng-container *ngIf="!loading && !errorMessage">
        <ng-container [ngSwitch]="view">
          <ng-container *ngSwitchCase="'ingresos'">
            <div class="card table-wrap" *ngIf="ingresos.length; else guardiaEmpty">
              <table class="table" data-testid="guardia-ingresos">
                <thead><tr><th>Vehículo</th><th>Conductor</th><th>Espacio</th><th>Ingreso</th><th>Estado</th><th>Pago / salida</th><th>Acción</th></tr></thead>
                <tbody>
                  <tr *ngFor="let ingreso of ingresos">
                    <td class="plate">{{ ingreso.placa }}</td><td>{{ ingreso.cliente }}</td>
                    <td class="mono">{{ ingreso.espacio }}</td><td>{{ ingreso.fechaIngreso }}</td>
                    <td><span class="badge" [class.badge-warning]="ingreso.estado === 'ESPERANDO'" [class.badge-available]="ingreso.estado === 'ACTIVO'">{{ ingreso.estado === 'ESPERANDO' ? 'Por validar' : 'En parqueadero' }}</span></td>
                    <td>
                      <ng-container *ngIf="ingreso.estado === 'ESPERANDO'; else estadoSalida">
                        <span class="badge badge-neutral">Pendiente de entrada</span>
                      </ng-container>
                      <ng-template #estadoSalida>
                        <span class="badge" [class.badge-available]="ingreso.puedeSalir" [class.badge-warning]="!ingreso.puedeSalir"
                              [attr.data-testid]="'guardia-pago-' + ingreso.id">
                          {{ ingreso.puedeSalir ? 'Pagado' : ingreso.estadoPago === 'TOLERANCIA_VENCIDA' ? 'Tolerancia vencida' : 'Pendiente de pago' }}
                        </span>
                        <small *ngIf="ingreso.puedeSalir && ingreso.fechaLimiteSalida" class="countdown"
                               [attr.data-testid]="'guardia-countdown-' + ingreso.id">
                          Tiene {{ minutosRestantes(ingreso) }} min para salir de la universidad
                        </small>
                        <small *ngIf="ingreso.estadoPago === 'TOLERANCIA_VENCIDA'" class="text-danger">
                          Debe pagar el tiempo adicional en pagaduría.
                        </small>
                        <small *ngIf="!ingreso.puedeSalir && ingreso.estadoPago !== 'TOLERANCIA_VENCIDA'" class="text-danger">
                          El conductor debe pagar en pagaduría.
                        </small>
                      </ng-template>
                    </td>
                    <td>
                      <button class="btn btn-primary btn-sm" type="button"
                              [disabled]="workingId === ingreso.id || (ingreso.estado === 'ACTIVO' && !ingreso.puedeSalir)"
                              (click)="procesar(ingreso)" [attr.data-testid]="'guardia-accion-' + ingreso.id">
                        {{ workingId === ingreso.id ? 'Procesando...' : ingreso.estado === 'ESPERANDO' ? 'Validar entrada' : 'Registrar salida' }}
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <form class="card card-body novelty" (ngSubmit)="registrarNovedad()" data-testid="guardia-novedad-form">
              <div class="form-header"><h2>Registrar novedad</h2></div>
              <div class="form-group">
                <label for="novedad-ingreso">Ingreso relacionado</label>
                <select id="novedad-ingreso" class="form-control" name="ingreso" [(ngModel)]="novedadIngresoId" required>
                  <option [ngValue]="null" disabled>Selecciona un vehículo</option>
                  <option *ngFor="let ingreso of ingresos" [ngValue]="ingreso.id">{{ ingreso.placa }} · {{ ingreso.cliente }}</option>
                </select>
              </div>
              <div class="form-group">
                <label for="novedad-descripcion">Descripción</label>
                <textarea id="novedad-descripcion" class="form-control" name="descripcion" rows="3"
                          [(ngModel)]="novedadDescripcion" maxlength="240" required
                          placeholder="Describe brevemente lo ocurrido"></textarea>
              </div>
              <button class="btn btn-secondary" type="submit"
                      [disabled]="!novedadIngresoId || !novedadDescripcion.trim() || savingNovedad"
                      data-testid="guardia-registrar-novedad">
                {{ savingNovedad ? 'Guardando...' : 'Guardar novedad' }}
              </button>
              <p *ngIf="novedades.length" class="text-muted" data-testid="guardia-novedad-reciente">
                Última novedad: {{ novedades[0].descripcion }}
              </p>
            </form>
          </ng-container>

          <ng-container *ngSwitchCase="'ocupacion'">
            <ng-container *ngIf="ocupacion && ocupacion.capacidad > 0; else guardiaEmpty">
              <article class="card card-body occupancy" data-testid="guardia-ocupacion">
                <div class="occupancy-head">
                  <div><span class="label">Parqueadero</span><h2>{{ ocupacion.parqueadero }}</h2></div>
                  <strong class="occupancy-total">{{ ocupacion.ocupados + ocupacion.reservados }} / {{ ocupacion.capacidad }}</strong>
                </div>
                <div class="progress" role="progressbar" [attr.aria-valuenow]="porcentajeOcupacion" aria-valuemin="0" aria-valuemax="100">
                  <span [style.width.%]="porcentajeOcupacion"></span>
                </div>
                <div class="metrics">
                  <div class="metric metric-occupied"><div class="label">Ocupados</div><div class="value">{{ ocupacion.ocupados }}</div></div>
                  <div class="metric metric-warning"><div class="label">Reservados</div><div class="value">{{ ocupacion.reservados }}</div></div>
                  <div class="metric metric-available"><div class="label">Disponibles</div><div class="value">{{ ocupacion.disponibles }}</div></div>
                </div>
              </article>
            </ng-container>
          </ng-container>
        </ng-container>
      </ng-container>

      <ng-template #guardiaEmpty>
        <div class="card state empty" data-testid="guardia-empty">
          <span class="material-symbols-outlined">inbox</span>
          <h2>Sin información para mostrar</h2>
          <p>Cuando haya datos disponibles, aparecerán aquí.</p>
        </div>
      </ng-template>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [`
    .page { max-width: 1180px; margin: 0 auto; }
    .state { min-height: 180px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; text-align: center; padding: 28px; }
    .state .material-symbols-outlined { font-size: 30px; color: var(--pf-muted); }
    .state h2 { font-size: 17px; }
    .state p, .text-muted { color: var(--pf-muted); }
    .empty { border-style: dashed; }
    .alert { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .alert-success { background: var(--pf-available-bg); border-color: var(--pf-available-border); color: var(--pf-available-text); }
    .novelty { max-width: 720px; margin-top: 20px; }
    .form-header h2 { font-size: 17px; }
    .novelty > .btn { margin-bottom: 14px; }
    .occupancy { max-width: 850px; }
    .occupancy-head { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .occupancy-head h2 { font-size: 20px; margin-top: 3px; }
    .label { color: var(--pf-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
    .occupancy-total { font-family: var(--pf-mono); font-size: 20px; }
    .progress { height: 12px; margin: 22px 0; overflow: hidden; background: var(--pf-surface-alt); border-radius: 99px; }
    .progress span { display: block; height: 100%; background: var(--pf-primary); border-radius: inherit; }
    .countdown { display: block; margin-top: 4px; color: var(--pf-primary); font-weight: 600; }
    .text-danger { display: block; margin-top: 4px; color: var(--pf-occupied-text); }
    @media (max-width: 600px) { .occupancy-head { align-items: flex-start; flex-direction: column; } }
  `]
})
export class GuardiaComponent implements OnDestroy {
  readonly view: GuardiaView;
  ingresos: IngresoOperativo[] = [];
  novedades: NovedadResponse[] = [];
  ocupacion: OcupacionOperativa | null = null;
  loading = true;
  errorMessage = '';
  successMessage = '';
  workingId: number | null = null;
  savingNovedad = false;
  novedadIngresoId: number | null = null;
  novedadDescripcion = '';
  now = Date.now();
  private readonly countdownTimer: ReturnType<typeof setInterval>;

  constructor(route: ActivatedRoute, private operaciones: OperacionesService) {
    this.view = route.snapshot.data['view'] as GuardiaView;
    this.countdownTimer = setInterval(() => { this.now = Date.now(); }, 1000);
    this.load();
  }

  ngOnDestroy(): void {
    clearInterval(this.countdownTimer);
  }

  minutosRestantes(ingreso: IngresoOperativo): number {
    if (!ingreso.fechaLimiteSalida) {
      return 0;
    }
    const limite = new Date(ingreso.fechaLimiteSalida).getTime();
    if (Number.isNaN(limite)) {
      return 0;
    }
    return Math.max(0, Math.ceil((limite - this.now) / 60000));
  }

  get porcentajeOcupacion(): number {
    if (!this.ocupacion || this.ocupacion.capacidad === 0) {
      return 0;
    }
    return Math.min(100, Math.round((this.ocupacion.ocupados + this.ocupacion.reservados) / this.ocupacion.capacidad * 100));
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    const onError = (): void => {
      this.loading = false;
      this.errorMessage = 'No se pudo cargar la operación. Verifica tu vínculo con el parqueadero.';
    };

    if (this.view === 'ingresos') {
      this.operaciones.getIngresosActivos().subscribe({
        next: value => {
          this.ingresos = value;
          this.loading = false;
          this.operaciones.getNovedades().subscribe({ next: items => { this.novedades = items; }, error: onError });
        },
        error: onError
      });
    } else {
      this.operaciones.getOcupacion().subscribe({
        next: value => { this.ocupacion = value; this.loading = false; },
        error: onError
      });
    }
  }

  procesar(ingreso: IngresoOperativo): void {
    const accion: AccionIngreso = ingreso.estado === 'ESPERANDO' ? 'VALIDAR_ENTRADA' : 'REGISTRAR_SALIDA';
    if (accion === 'REGISTRAR_SALIDA' && ingreso.puedeSalir === false) {
      this.errorMessage = ingreso.estadoPago === 'TOLERANCIA_VENCIDA'
        ? 'La tolerancia de salida venció; debe cancelar el tiempo adicional en pagaduría.'
        : 'El conductor debe pagar en pagaduría antes de salir del parqueadero.';
      return;
    }

    this.workingId = ingreso.id;
    this.successMessage = '';
    this.errorMessage = '';
    this.operaciones.actualizarIngreso(ingreso.id, accion).subscribe({
      next: () => {
        this.workingId = null;
        this.successMessage = accion === 'VALIDAR_ENTRADA'
          ? `Entrada de ${ingreso.placa} validada.`
          : `Salida de ${ingreso.placa} registrada.`;
        this.load();
      },
      error: () => {
        this.workingId = null;
        this.errorMessage = 'No se pudo actualizar el ingreso. Verifica tus permisos y el parqueadero asignado.';
      }
    });
  }

  registrarNovedad(): void {
    const ingresoId = this.novedadIngresoId;
    const descripcion = this.novedadDescripcion.trim();
    if (ingresoId === null || !descripcion) {
      return;
    }
    this.savingNovedad = true;
    this.errorMessage = '';
    this.operaciones.registrarNovedad({ ingresoId, descripcion }).subscribe({
      next: novedad => {
        this.novedades = [novedad, ...this.novedades];
        this.novedadDescripcion = '';
        this.savingNovedad = false;
        this.successMessage = 'Novedad registrada.';
      },
      error: () => {
        this.savingNovedad = false;
        this.errorMessage = 'No se pudo registrar la novedad. Verifica tus permisos y el ingreso seleccionado.';
      }
    });
  }
}
