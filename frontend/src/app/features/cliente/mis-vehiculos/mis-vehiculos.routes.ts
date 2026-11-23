import { Routes } from '@angular/router';

export const MIS_VEHICULOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/mis-vehiculos-list/mis-vehiculos-list.component')
      .then(m => m.MisVehiculosListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/mis-vehiculos-form/mis-vehiculos-form.component')
      .then(m => m.MisVehiculosFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/mis-vehiculos-form/mis-vehiculos-form.component')
      .then(m => m.MisVehiculosFormComponent)
  }
];
