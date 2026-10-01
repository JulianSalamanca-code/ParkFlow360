import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TarifaService } from '../../services/tarifa.service';
import { Tarifa } from '../../models/tarifa.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-tarifa-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Tarifas</h1>
          <p>Configuración de precios por tiempo de permanencia</p>
        </div>
        <a routerLink="/tarifas/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">add</span> Nueva Tarifa
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Tarifas Activas</div>
          <div class="value">{{ tarifas.length }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Tipo</th>
                <th>Valor</th>
                <th>Duración</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let tarifa of tarifas">
                <td><strong>{{ tarifa.nombre }}</strong></td>
                <td><span class="badge badge-neutral">{{ tarifa.tipo }}</span></td>
                <td><span class="num">{{ tarifa.valor | currency:'COP':'symbol-narrow':'1.0-0' }}</span></td>
                <td>{{ tarifa.duracion }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/tarifas/editar', tarifa.id]" class="btn btn-sm btn-secondary">
                      <span class="material-symbols-outlined">edit</span> Editar
                    </a>
                    <button (click)="eliminar(tarifa.id)" class="btn btn-sm btn-danger">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="tarifas.length === 0">
                <td colspan="5" class="table-empty">No hay tarifas registradas</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class TarifaListComponent implements OnInit {
  tarifas: Tarifa[] = [];

  constructor(private tarifaService: TarifaService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargarTarifas();
  }

  cargarTarifas(): void {
    this.tarifaService.listarTodos().subscribe({
      next: (data) => this.tarifas = data,
      error: (err) => console.error('Error al cargar tarifas:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar esta tarifa?')) {
      this.tarifaService.eliminar(id).subscribe({
        next: () => {
          this.toast.success('Tarifa eliminada correctamente.');
          this.cargarTarifas();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar la tarifa.')
      });
    }
  }
}
