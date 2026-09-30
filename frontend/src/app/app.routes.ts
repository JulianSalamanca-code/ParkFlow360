import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'vehiculos',
    loadChildren: () => import('./features/vehiculos/vehiculos.routes')
      .then(m => m.VEHICULOS_ROUTES)
  },
  {
    path: '',
    redirectTo: 'vehiculos',
    pathMatch: 'full'
  }
];
