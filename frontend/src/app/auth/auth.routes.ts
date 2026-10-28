import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'registro',
    loadComponent: () => import('./components/registro/registro.component')
      .then(m => m.RegistroComponent)
  },
  {
    path: 'google/callback',
    loadComponent: () => import('./components/google-callback/google-callback.component')
      .then(m => m.GoogleCallbackComponent)
  }
];
