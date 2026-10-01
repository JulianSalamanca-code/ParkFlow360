import { Routes } from '@angular/router';

export const PAGOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/pago-list/pago-list.component')
      .then(m => m.PagoListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/pago-form/pago-form.component')
      .then(m => m.PagoFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/pago-form/pago-form.component')
      .then(m => m.PagoFormComponent)
  }
];
