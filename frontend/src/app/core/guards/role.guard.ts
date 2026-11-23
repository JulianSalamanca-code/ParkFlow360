import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { Rol, normalizeRol } from '../auth/roles';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/auth/login']);
    }

    const rolesPermitidos = ((route.data['roles'] as string[] | undefined) ?? [])
      .map(rol => normalizeRol(rol))
      .filter((rol): rol is Rol => rol !== null);

    if (rolesPermitidos.length === 0 || this.authService.hasAnyRole(rolesPermitidos)) {
      return true;
    }

    // Cada rol vuelve a su pantalla inicial; evita bucles de redirección.
    return this.router.createUrlTree([this.authService.homeRoute()]);
  }
}
