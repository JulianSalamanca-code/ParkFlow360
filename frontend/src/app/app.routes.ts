import { Routes } from '@angular/router';
import { RoleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes')
      .then(m => m.AUTH_ROUTES)
  },

  // ---------------- PLATAFORMA ----------------
  // La plataforma NO registra vehículos ni reserva: opera y supervisa.
  {
    path: 'planos',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/planos/planos.routes')
      .then(m => m.PLANOS_ROUTES)
  },
  {
    path: 'espacios',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/espacios/espacios.routes')
      .then(m => m.ESPACIOS_ROUTES)
  },
  {
    path: 'tarifas',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/tarifas/tarifas.routes')
      .then(m => m.TARIFAS_ROUTES)
  },
  {
    path: 'ingresos',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/ingresos/ingresos.routes')
      .then(m => m.INGRESOS_ROUTES)
  },
  {
    path: 'pagos',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/pagos/pagos.routes')
      .then(m => m.PAGOS_ROUTES)
  },
  {
    path: 'reportes',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/reportes/reportes.routes')
      .then(m => m.REPORTES_ROUTES)
  },
  {
    path: 'usuarios',
    canActivate: [RoleGuard], data: { roles: ['PLATAFORMA'] },
    loadChildren: () => import('./features/usuarios/usuarios.routes')
      .then(m => m.USUARIOS_ROUTES)
  },

  // ---------------- EMPRESA ----------------
  {
    path: 'empresa',
    canActivate: [RoleGuard], data: { roles: ['EMPRESA'], view: 'resumen' },
    loadComponent: () => import('./features/operaciones/empresa/empresa.component')
      .then(m => m.EmpresaComponent)
  },
  {
    path: 'empresa/parqueaderos',
    canActivate: [RoleGuard], data: { roles: ['EMPRESA'], view: 'parqueaderos' },
    loadComponent: () => import('./features/operaciones/empresa/empresa.component')
      .then(m => m.EmpresaComponent)
  },
  {
    path: 'empresa/reportes',
    canActivate: [RoleGuard], data: { roles: ['EMPRESA'], view: 'reportes' },
    loadComponent: () => import('./features/operaciones/empresa/empresa.component')
      .then(m => m.EmpresaComponent)
  },
  {
    path: 'empresa/configuracion',
    canActivate: [RoleGuard], data: { roles: ['EMPRESA'], view: 'configuracion' },
    loadComponent: () => import('./features/operaciones/empresa/empresa.component')
      .then(m => m.EmpresaComponent)
  },

  // ---------------- CAJERO DE PAGOS ----------------
  {
    path: 'cajero/pagos',
    canActivate: [RoleGuard], data: { roles: ['CAJERO_PAGOS'], view: 'pendientes' },
    loadComponent: () => import('./features/operaciones/cajero/cajero.component')
      .then(m => m.CajeroComponent)
  },
  {
    path: 'cajero/historial',
    canActivate: [RoleGuard], data: { roles: ['CAJERO_PAGOS'], view: 'historial' },
    loadComponent: () => import('./features/operaciones/cajero/cajero.component')
      .then(m => m.CajeroComponent)
  },

  // ---------------- GUARDIA ----------------
  {
    path: 'guardia/ingresos',
    canActivate: [RoleGuard], data: { roles: ['GUARDIA'], view: 'ingresos' },
    loadComponent: () => import('./features/operaciones/guardia/guardia.component')
      .then(m => m.GuardiaComponent)
  },
  {
    path: 'guardia/ocupacion',
    canActivate: [RoleGuard], data: { roles: ['GUARDIA'], view: 'ocupacion' },
    loadComponent: () => import('./features/operaciones/guardia/guardia.component')
      .then(m => m.GuardiaComponent)
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
    path: 'pagar/resultado',
    canActivate: [RoleGuard], data: { roles: ['USUARIO'] },
    loadComponent: () => import('./features/cliente/pagar/pagar-resultado.component')
      .then(m => m.PagarResultadoComponent)
  },
  {
    path: 'pagar',
    canActivate: [RoleGuard], data: { roles: ['USUARIO'] },
    loadComponent: () => import('./features/cliente/pagar/pagar.component')
      .then(m => m.PagarComponent)
  },

  // ---------------- ROLES EN PREPARACIÓN ----------------
  {
    path: 'proximamente',
    canActivate: [RoleGuard], data: { roles: [] },
    loadComponent: () => import('./features/proximamente/proximamente.component')
      .then(m => m.ProximamenteComponent)
  },

  {
    path: '',
    loadComponent: () => import('./core/home-redirect.component')
      .then(m => m.HomeRedirectComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
