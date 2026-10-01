import { Routes } from '@angular/router';

export const INGRESOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/ingreso-list/ingreso-list.component')
      .then(m => m.IngresoListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/ingreso-form/ingreso-form.component')
      .then(m => m.IngresoFormComponent)
  }
];
