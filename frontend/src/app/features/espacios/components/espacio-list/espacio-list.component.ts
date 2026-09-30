import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EspacioService } from '../../services/espacio.service';
import { Espacio } from '../../models/espacio.model';

@Component({
  selector: 'app-espacio-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="espacio-list">
      <div class="header">
        <h2>Espacios</h2>
        <a routerLink="/espacios/nuevo" class="btn btn-primary">Nuevo Espacio</a>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Número</th>
            <th>Tipo</th>
            <th>Estado</th>
            <th>Piso</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let espacio of espacios">
            <td>{{ espacio.numero }}</td>
            <td>{{ espacio.tipo }}</td>
            <td>
              <span class="badge" [class]="getEstadoClass(espacio.estado)">
                {{ espacio.estado }}
              </span>
            </td>
            <td>{{ espacio.piso }}</td>
            <td>
              <a [routerLink]="['/espacios/editar', espacio.id]" class="btn btn-sm btn-warning">Editar</a>
              <button (click)="eliminar(espacio.id)" class="btn btn-sm btn-danger">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .espacio-list { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    .table th { background-color: #f5f5f5; font-weight: bold; }
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
    .badge-libre { background-color: #28a745; color: white; }
    .badge-ocupado { background-color: #dc3545; color: white; }
    .badge-reservado { background-color: #ffc107; color: black; }
    .badge-mantenimiento { background-color: #6c757d; color: white; }
    .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }
    .btn-primary { background-color: #007bff; color: white; }
    .btn-warning { background-color: #ffc107; color: black; }
    .btn-danger { background-color: #dc3545; color: white; }
    .btn-sm { padding: 4px 8px; font-size: 12px; margin-right: 4px; }
  `]
})
export class EspacioListComponent implements OnInit {
  espacios: Espacio[] = [];

  constructor(private espacioService: EspacioService) {}

  ngOnInit(): void {
    this.cargarEspacios();
  }

  cargarEspacios(): void {
    this.espacioService.listarTodos().subscribe({
      next: (data) => this.espacios = data,
      error: (err) => console.error('Error al cargar espacios:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este espacio?')) {
      this.espacioService.eliminar(id).subscribe({
        next: () => this.cargarEspacios(),
        error: (err) => console.error('Error al eliminar:', err)
      });
    }
  }

  getEstadoClass(estado: string): string {
    switch (estado.toUpperCase()) {
      case 'LIBRE': return 'badge-libre';
      case 'OCUPADO': return 'badge-ocupado';
      case 'RESERVADO': return 'badge-reservado';
      case 'MANTENIMIENTO': return 'badge-mantenimiento';
      default: return '';
    }
  }
}
