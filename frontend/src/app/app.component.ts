import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  isAuthenticated$: Observable<boolean>;

  constructor(private authService: AuthService, private router: Router) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
  }

  get userName(): string {
    return this.authService.getUser()?.nombre || '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
