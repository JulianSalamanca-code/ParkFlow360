import { Routes } from '@angular/router';

export const VEHICULOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/vehiculo-list/vehiculo-list.component')
      .then(m => m.VehiculoListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/vehiculo-form/vehiculo-form.component')
      .then(m => m.VehiculoFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/vehiculo-form/vehiculo-form.component')
      .then(m => m.VehiculoFormComponent)
  }
];
