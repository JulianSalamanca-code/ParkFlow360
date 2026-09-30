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
    path: '',
    redirectTo: 'vehiculos',
    pathMatch: 'full'
  }
];
