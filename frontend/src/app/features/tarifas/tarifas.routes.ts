import { Routes } from '@angular/router';

export const TARIFAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/tarifa-list/tarifa-list.component')
      .then(m => m.TarifaListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/tarifa-form/tarifa-form.component')
      .then(m => m.TarifaFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/tarifa-form/tarifa-form.component')
      .then(m => m.TarifaFormComponent)
  }
];
