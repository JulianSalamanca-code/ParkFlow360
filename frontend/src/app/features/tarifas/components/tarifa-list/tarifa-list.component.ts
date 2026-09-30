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
    <div class="tarifa-list">
      <div class="header">
        <h2>Tarifas</h2>
        <a routerLink="/tarifas/nuevo" class="btn btn-primary">Nueva Tarifa</a>
      </div>

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
            <td>{{ tarifa.nombre }}</td>
            <td>{{ tarifa.tipo }}</td>
            <td>{{ tarifa.valor | currency }}</td>
            <td>{{ tarifa.duracion }}</td>
            <td>
              <a [routerLink]="['/tarifas/editar', tarifa.id]" class="btn btn-sm btn-warning">Editar</a>
              <button (click)="eliminar(tarifa.id)" class="btn btn-sm btn-danger">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .tarifa-list { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    .table th { background-color: #f5f5f5; font-weight: bold; }
    .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-warning { background-color: #ffc107; color: black; }
    .btn-danger { background-color: #dc3545; color: white; }
    .btn-sm { padding: 4px 8px; font-size: 12px; margin-right: 4px; }
  `]
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
