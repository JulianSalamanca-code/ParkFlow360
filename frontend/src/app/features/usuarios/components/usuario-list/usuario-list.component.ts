import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { Usuario } from '../../models/usuario.model';
import { ToastService } from '../../../../shared/services/toast.service';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Usuarios</h1>
          <p>Administración de cuentas y roles del sistema</p>
        </div>
        <a routerLink="/usuarios/nuevo" class="btn btn-primary">
          <span class="material-symbols-outlined">person_add</span> Nuevo Usuario
        </a>
      </div>

      <div class="metrics">
        <div class="metric">
          <div class="label">Total Usuarios</div>
          <div class="value">{{ usuarios.length }}</div>
        </div>
        <div class="metric metric-available">
          <div class="label">Administradores</div>
          <div class="value">{{ contarPorRol('ADMIN') }}</div>
        </div>
        <div class="metric metric-warning">
          <div class="label">Clientes</div>
          <div class="value">{{ contarPorRol('USUARIO') }}</div>
        </div>
      </div>

      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Rol</th>
                <th>Creado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let usuario of usuarios">
                <td><strong>{{ usuario.nombre }}</strong></td>
                <td>{{ usuario.email }}</td>
                <td>
                  <span class="badge" [ngClass]="usuario.rol === 'ADMIN' ? 'badge-available' : 'badge-warning'">
                    {{ usuario.rol }}
                  </span>
                </td>
                <td class="mono">{{ usuario.fechaCreacion | date:'short' }}</td>
                <td>
                  <div class="table-actions">
                    <a [routerLink]="['/usuarios/editar', usuario.id]" class="btn btn-sm btn-secondary">
                      <span class="material-symbols-outlined">edit</span> Editar
                    </a>
                    <button (click)="eliminar(usuario.id)" class="btn btn-sm btn-danger">
                      <span class="material-symbols-outlined">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="usuarios.length === 0">
                <td colspan="5" class="table-empty">No hay usuarios registrados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class UsuarioListComponent implements OnInit {
  usuarios: Usuario[] = [];

  constructor(private usuarioService: UsuarioService, private toast: ToastService) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.usuarioService.listarTodos().subscribe({
      next: (data) => this.usuarios = data,
      error: (err) => console.error('Error al cargar usuarios:', err)
    });
  }

  contarPorRol(rol: string): number {
    return this.usuarios.filter(u => u.rol?.toUpperCase() === rol).length;
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este usuario?')) {
      this.usuarioService.eliminar(id).subscribe({
        next: () => {
          this.toast.success('Usuario eliminado correctamente.');
          this.cargarUsuarios();
        },
        error: (err) => this.toast.error(err.error?.message || 'No se pudo eliminar el usuario.')
      });
    }
  }
}
