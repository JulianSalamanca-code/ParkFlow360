import { Routes } from '@angular/router';
import { RoleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes')
      .then(m => m.AUTH_ROUTES)
  },

  // ---------------- ADMINISTRADOR ----------------
  {
    path: 'vehiculos',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/vehiculos/vehiculos.routes')
      .then(m => m.VEHICULOS_ROUTES)
  },
  {
    path: 'espacios',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/espacios/espacios.routes')
      .then(m => m.ESPACIOS_ROUTES)
  },
  {
    path: 'tarifas',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/tarifas/tarifas.routes')
      .then(m => m.TARIFAS_ROUTES)
  },
  {
    path: 'ingresos',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/ingresos/ingresos.routes')
      .then(m => m.INGRESOS_ROUTES)
  },
  {
    path: 'pagos',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/pagos/pagos.routes')
      .then(m => m.PAGOS_ROUTES)
  },
  {
    path: 'reportes',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/reportes/reportes.routes')
      .then(m => m.REPORTES_ROUTES)
  },
  {
    path: 'usuarios',
    canActivate: [RoleGuard], data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/usuarios/usuarios.routes')
      .then(m => m.USUARIOS_ROUTES)
  },

  // ---------------- USUARIO (cliente) ----------------
  {
    path: 'reservar',
    canActivate: [RoleGuard], data: { roles: ['USUARIO'] },
    loadComponent: () => import('./features/cliente/reservar/reservar.component')
      .then(m => m.ReservarComponent)
  },
  {
    path: 'mis-parqueos',
    canActivate: [RoleGuard], data: { roles: ['USUARIO'] },
    loadComponent: () => import('./features/cliente/mis-parqueos/mis-parqueos.component')
      .then(m => m.MisParqueosComponent)
  },
  {
    path: 'mis-vehiculos',
    canActivate: [RoleGuard], data: { roles: ['USUARIO'] },
    loadChildren: () => import('./features/cliente/mis-vehiculos/mis-vehiculos.routes')
      .then(m => m.MIS_VEHICULOS_ROUTES)
  },

  {
    path: '',
    redirectTo: 'vehiculos',
    pathMatch: 'full'
  }
];
