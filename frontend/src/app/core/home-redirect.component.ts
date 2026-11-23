import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';

@Component({
  standalone: true,
  template: '',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class HomeRedirectComponent implements OnInit {
  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    void this.router.navigateByUrl(this.authService.homeRoute(), { replaceUrl: true });
  }
}
