import { Routes } from '@angular/router';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/usuario-list/usuario-list.component')
      .then(m => m.UsuarioListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/usuario-form/usuario-form.component')
      .then(m => m.UsuarioFormComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/usuario-form/usuario-form.component')
      .then(m => m.UsuarioFormComponent)
  }
];
