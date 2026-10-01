import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IngresoService } from '../../services/ingreso.service';
import { Ingreso } from '../../models/ingreso.model';
import { TicketComponent } from '../../../../shared/components/ticket/ticket.component';
import { ToastService } from '../../../../shared/services/toast.service';

type Filtro = 'TODOS' | 'EN_USO' | 'FINALIZADOS';

@Component({
  selector: 'app-ingreso-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TicketComponent],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Ingresos</h1>
          <p>Selecciona un registro para ver su ticket</p>
        </div>
        <a routerLink="/ingresos/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Registrar Entrada
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Total Registros</div>
          <div class="value">{{ ingresos.length }}</div>
        </div>
        <div class="metric metric-occupied">
          <div class="label">En Parqueadero</div>
          <div class="value">{{ contarPorEstado('ACTIVO') }}</div>
        </div>
        <div class="metric metric-warning">
          <div class="label">Reservados</div>
          <div class="value">{{ contarPorEstado('RESERVADO') }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Finalizados</div>
          <div class="value">{{ contarPorEstado('FINALIZADO') }}</div>
        </div>
      </div>

      <div class="filter-bar">
        <button class="filter-btn" [class.active]="filtro === 'TODOS'" (click)="filtro = 'TODOS'">
          Todos ({{ ingresos.length }})
        </button>
        <button class="filter-btn" [class.active]="filtro === 'EN_USO'" (click)="filtro = 'EN_USO'">
          En Uso ({{ enUso.length }})
        </button>
        <button class="filter-btn" [class.active]="filtro === 'FINALIZADOS'" (click)="filtro = 'FINALIZADOS'">
          Finalizados ({{ contarPorEstado('FINALIZADO') }})
        </button>
      </div>

      <div class="split">
        <!-- Columna izquierda: tabla -->
        <div class="card">
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>Folio</th>
                  <th>Placa</th>
                  <th>Espacio</th>
                  <th>Estado</th>
                  <th>Entrada</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let ingreso of ingresosFiltrados"
                    class="row-click" [class.selected]="seleccionado?.id === ingreso.id"
                    (click)="seleccionar(ingreso)">
                  <td><span class="plate">{{ ingreso.folio }}</span></td>
                  <td><span class="plate">{{ ingreso.placa }}</span></td>
                  <td class="num">{{ ingreso.espacioNumero || ingreso.espacioId }}</td>
                  <td><span class="badge" [ngClass]="estadoBadge(ingreso.estado)">{{ ingreso.estado }}</span></td>
                  <td class="mono">{{ ingreso.fechaEntrada | date:'short' }}</td>
                  <td (click)="$event.stopPropagation()">
                    <div class="table-actions">
                      <button *ngIf="ingreso.estado !== 'FINALIZADO'"
                              (click)="registrarSalida(ingreso.id)" class="btn btn-sm btn-primary"
                              title="Registrar salida">
                        <span class="material-symbols-outlined">logout</span>
                      </button>
                      <button (click)="eliminar(ingreso.id)" class="btn btn-sm btn-danger" title="Eliminar">
                        <span class="material-symbols-outlined">delete</span>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr *ngIf="ingresosFiltrados.length === 0">
                  <td colspan="6" class="table-empty">No hay registros para este filtro</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Columna derecha: ticket -->
        <div class="ticket-col">
          <div class="ticket-head">
            <span class="material-symbols-outlined">receipt_long</span>
            <strong>Ticket</strong>
            <button *ngIf="seleccionado" class="btn btn-sm btn-secondary" style="margin-left:auto" (click)="imprimir()">
              <span class="material-symbols-outlined">print</span> Imprimir
            </button>
          </div>

          <app-ticket
            *ngIf="seleccionado"
            [titulo]="seleccionado.estado === 'RESERVADO' ? 'RESERVA' : 'TICKET INGRESO'"
            [folio]="seleccionado.folio"
            [placa]="seleccionado.placa"
            [categoria]="seleccionado.categoria"
            [espacio]="espacioTexto(seleccionado)"
            [tarifaValor]="seleccionado.tarifaValor ?? null"
            [modalidad]="seleccionado.modalidad || ''"
            [operador]="seleccionado.operador || ''"
            [novedad]="seleccionado.novedad || ''"
            [fecha]="seleccionado.fechaEntrada">
          </app-ticket>

          <div *ngIf="!seleccionado" class="ticket-placeholder">
            <span class="material-symbols-outlined">receipt_long</span>
            <p>Selecciona un registro de la lista para ver su ticket.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .filter-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
    .filter-btn {
      padding: 8px 14px; border: 1px solid var(--pf-outline-soft);
      border-radius: var(--pf-radius); background: #fff; cursor: pointer;
      font-weight: 600; font-size: 13px; color: var(--pf-ink-soft); transition: all 0.15s;
    }
    .filter-btn:hover { background: var(--pf-surface-alt); }
    .filter-btn.active { background: var(--pf-primary); border-color: var(--pf-primary); color: #fff; }

    .split { display: grid; grid-template-columns: 1fr 400px; gap: 20px; align-items: start; }
    .row-click { cursor: pointer; }
    .row-click.selected { background: var(--pf-primary-container) !important; }

    .ticket-col { display: flex; flex-direction: column; align-items: center; }
    .ticket-head {
      display: flex; align-items: center; gap: 8px; width: 100%;
      margin-bottom: 12px; font-size: 14px;
    }
    .ticket-head .material-symbols-outlined { color: var(--pf-primary); }
    .ticket-placeholder {
      width: 100%; max-width: 360px; min-height: 320px;
      border: 2px dashed var(--pf-outline); border-radius: var(--pf-radius);
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      gap: 10px; color: var(--pf-muted); text-align: center; padding: 24px;
    }
    .ticket-placeholder .material-symbols-outlined { font-size: 40px; }

    @media (max-width: 1100px) {
      .split { grid-template-columns: 1fr; }
    }
  `]
})
export class IngresoListComponent implements OnInit {
  ingresos: Ingreso[] = [];
  filtro: Filtro = 'TODOS';
  seleccionado: Ingreso | null = null;

  constructor(private ingresoService: IngresoService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargarIngresos();
  }

  get enUso(): Ingreso[] {
    return this.ingresos.filter(i => {
      const e = i.estado?.toUpperCase();
      return e === 'ACTIVO' || e === 'RESERVADO';
    });
  }

  get ingresosFiltrados(): Ingreso[] {
    switch (this.filtro) {
      case 'EN_USO': return this.enUso;
      case 'FINALIZADOS': return this.ingresos.filter(i => i.estado?.toUpperCase() === 'FINALIZADO');
      default: return this.ingresos;
    }
  }

  cargarIngresos(): void {
    this.ingresoService.listarTodos().subscribe({
      next: (data) => {
        this.ingresos = data;
        // Mantener seleccionado el registro actualizado
        if (this.seleccionado) {
          this.seleccionado = data.find(i => i.id === this.seleccionado!.id) || null;
        }
      },
      error: (err) => console.error('Error al cargar ingresos:', err)
    });
  }

  seleccionar(ingreso: Ingreso): void {
    this.seleccionado = ingreso;
  }

  espacioTexto(ingreso: Ingreso): string {
    return ingreso.espacioNumero || String(ingreso.espacioId || '---');
  }

  contarPorEstado(estado: string): number {
    return this.ingresos.filter(i => i.estado?.toUpperCase() === estado).length;
  }

  estadoBadge(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'ACTIVO': return 'badge-occupied';
      case 'RESERVADO': return 'badge-warning';
      case 'FINALIZADO': return 'badge-available';
      default: return 'badge-neutral';
    }
  }

  registrarSalida(id: number): void {
    if (confirm('¿Registrar la salida de este vehículo?')) {
      this.ingresoService.registrarSalida(id).subscribe({
        next: () => {
          this.toast.success('Salida registrada correctamente.');
          this.cargarIngresos();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo registrar la salida.')
      });
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este registro?')) {
      this.ingresoService.eliminar(id).subscribe({
        next: () => {
          this.toast.success('Registro eliminado correctamente.');
          this.cargarIngresos();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el registro.')
      });
    }
  }

  imprimir(): void {
    window.print();
  }
}
