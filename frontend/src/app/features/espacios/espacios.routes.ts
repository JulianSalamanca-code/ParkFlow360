import { Routes } from '@angular/router';

export const ESPACIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/espacio-list/espacio-list.component')
      .then(m => m.EspacioListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/espacio-form/espacio-form.component')
      .then(m => m.EspacioFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/espacio-form/espacio-form.component')
      .then(m => m.EspacioFormComponent)
  }
];
