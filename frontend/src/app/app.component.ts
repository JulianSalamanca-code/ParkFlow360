import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { Observable, Subscription, interval } from 'rxjs';
import { AuthService } from './auth/services/auth.service';
import { ToastContainerComponent } from './shared/components/toast-container/toast-container.component';
import { ToastService } from './shared/services/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ToastContainerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  isAuthenticated$: Observable<boolean>;
  isAdmin$: Observable<boolean>;
  isUsuario$: Observable<boolean>;
  mobileNavOpen = false;
  clock = '';
  today = '';
  private clockSub?: Subscription;

  constructor(private authService: AuthService, private router: Router, private toast: ToastService) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.isAdmin$ = this.authService.isAdmin$;
    this.isUsuario$ = this.authService.isUsuario$;
  }

  get userName(): string {
    return this.authService.getUser()?.nombre || 'Operador';
  }

  get userRole(): string {
    return this.authService.getUser()?.rol || 'Usuario';
  }

  ngOnInit(): void {
    this.tick();
    this.clockSub = interval(1000).subscribe(() => this.tick());
  }

  ngOnDestroy(): void {
    this.clockSub?.unsubscribe();
  }

  private tick(): void {
    const now = new Date();
    this.clock = now.toLocaleTimeString('es-CO', { hour12: false });
    this.today = now.toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  logout(): void {
    this.authService.logout();
    this.toast.info('Sesión cerrada.');
    this.router.navigate(['/auth/login']);
  }

  toggleMobileNav(): void {
    this.mobileNavOpen = !this.mobileNavOpen;
  }

  closeMobileNav(): void {
    this.mobileNavOpen = false;
  }
}
