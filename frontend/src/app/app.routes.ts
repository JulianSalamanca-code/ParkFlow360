import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes')
      .then(m => m.AUTH_ROUTES)
  },
  {
    path: 'vehiculos',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/vehiculos/vehiculos.routes')
      .then(m => m.VEHICULOS_ROUTES)
  },
  {
    path: 'espacios',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/espacios/espacios.routes')
      .then(m => m.ESPACIOS_ROUTES)
  },
  {
    path: 'tarifas',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/tarifas/tarifas.routes')
      .then(m => m.TARIFAS_ROUTES)
  },
  {
    path: 'pagos',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/pagos/pagos.routes')
      .then(m => m.PAGOS_ROUTES)
  },
  {
    path: 'reportes',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/reportes/reportes.routes')
      .then(m => m.REPORTES_ROUTES)
  },
  {
    path: '',
    redirectTo: 'vehiculos',
    pathMatch: 'full'
  }
];
