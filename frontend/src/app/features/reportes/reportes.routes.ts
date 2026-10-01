import { Routes } from '@angular/router';

export const REPORTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/reporte-list/reporte-list.component')
      .then(m => m.ReporteListComponent)
  }
];
