import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/auth/login']);
    }

    const rolesPermitidos = (route.data['roles'] as string[]) || [];
    const rolActual = this.authService.getRol().toUpperCase();

    if (rolesPermitidos.length === 0 || rolesPermitidos.includes(rolActual)) {
      return true;
    }

    // Redirigir a la pantalla inicial según el rol
    return this.router.createUrlTree([this.authService.isAdmin() ? '/planos' : '/mis-parqueos']);
  }
}
