import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TarifaService } from '../../services/tarifa.service';
import { Tarifa } from '../../models/tarifa.model';

@Component({
  selector: 'app-tarifa-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <div>
            <h1>Tarifas</h1>
            <p class="page-subtitle">Configuración de precios por tiempo de permanencia</p>
          </div>
        </div>
        <a routerLink="/tarifas/nuevo" class="btn btn-primary">+ Nueva Tarifa</a>
      </div>

      <div class="card">
        <div class="table-wrapper">
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
                <td>{{ tarifa.tipo }}</td>
                <td>{{ tarifa.valor | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
                <td>{{ tarifa.duracion }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/tarifas/editar', tarifa.id]" class="btn btn-sm btn-warning">Editar</a>
                    <button (click)="eliminar(tarifa.id)" class="btn btn-sm btn-danger">Eliminar</button>
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

  constructor(private tarifaService: TarifaService) {}

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
        next: () => this.cargarTarifas(),
        error: (err) => console.error('Error al eliminar:', err)
      });
    }
  }
}
