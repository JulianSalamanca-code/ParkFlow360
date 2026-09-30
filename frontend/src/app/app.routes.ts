import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'vehiculos',
    loadChildren: () => import('./features/vehiculos/vehiculos.routes')
      .then(m => m.VEHICULOS_ROUTES)
  },
  {
    path: 'espacios',
    loadChildren: () => import('./features/espacios/espacios.routes')
      .then(m => m.ESPACIOS_ROUTES)
  },
  {
    path: 'tarifas',
    loadChildren: () => import('./features/tarifas/tarifas.routes')
      .then(m => m.TARIFAS_ROUTES)
  },
  {
    path: 'pagos',
    loadChildren: () => import('./features/pagos/pagos.routes')
      .then(m => m.PAGOS_ROUTES)
  },
  {
    path: '',
    redirectTo: 'vehiculos',
    pathMatch: 'full'
  }
];
