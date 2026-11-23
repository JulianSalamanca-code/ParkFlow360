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
    path: 'recuperar',
    loadComponent: () => import('./components/recuperar/recuperar.component')
      .then(m => m.RecuperarComponent)
  },
  {
    path: 'restablecer/:token',
    loadComponent: () => import('./components/restablecer/restablecer.component')
      .then(m => m.RestablecerComponent)
  },
  {
    path: 'google/callback',
    loadComponent: () => import('./components/google-callback/google-callback.component')
      .then(m => m.GoogleCallbackComponent)
  }
];
