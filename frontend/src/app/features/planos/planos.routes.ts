import { Routes } from '@angular/router';

export const PLANOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/plano-list/plano-list.component')
      .then(m => m.PlanoListComponent)
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./components/plano-editor/plano-editor.component')
      .then(m => m.PlanoEditorComponent)
  },
  {
    path: 'editar/:id',
    loadComponent: () => import('./components/plano-editor/plano-editor.component')
      .then(m => m.PlanoEditorComponent)
  },
  {
    path: ':id/mapa',
    loadComponent: () => import('./components/plano-mapa/plano-mapa.component')
      .then(m => m.PlanoMapaComponent)
  }
];
