import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { Observable, Subscription, interval } from 'rxjs';
import { AuthService } from './auth/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  isAuthenticated$: Observable<boolean>;
  clock = '';
  today = '';
  private clockSub?: Subscription;

  constructor(private authService: AuthService, private router: Router) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
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
    this.router.navigate(['/auth/login']);
  }
}
