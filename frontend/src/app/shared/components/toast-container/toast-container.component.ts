import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastType } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let t of toasts$ | async" class="toast" [ngClass]="'toast-' + t.type">
        <span class="material-symbols-outlined toast-icon">{{ icono(t.type) }}</span>
        <span class="toast-msg">{{ t.message }}</span>
        <button class="toast-close" (click)="dismiss(t.id)" title="Cerrar">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 380px;
      pointer-events: none;
    }

    .toast {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 14px 16px;
      border-radius: 8px;
      background: #ffffff;
      border: 1px solid var(--pf-outline-soft);
      border-left: 4px solid var(--pf-primary);
      box-shadow: 0 10px 28px rgba(15, 23, 42, 0.18);
      pointer-events: auto;
      animation: toast-in 0.25s ease-out;
    }

    @keyframes toast-in {
      from { opacity: 0; transform: translateX(30px); }
      to { opacity: 1; transform: translateX(0); }
    }

    .toast-success { border-left-color: var(--pf-available); }
    .toast-success .toast-icon { color: var(--pf-available); }
    .toast-error { border-left-color: var(--pf-occupied); }
    .toast-error .toast-icon { color: var(--pf-occupied); }
    .toast-info { border-left-color: var(--pf-primary); }
    .toast-info .toast-icon { color: var(--pf-primary); }

    .toast-icon { font-size: 22px; flex-shrink: 0; }

    .toast-msg {
      flex: 1;
      font-size: 14px;
      font-weight: 600;
      color: var(--pf-ink);
      padding-top: 1px;
      line-height: 1.4;
    }

    .toast-close {
      background: transparent;
      border: none;
      cursor: pointer;
      color: var(--pf-muted);
      padding: 0;
      display: flex;
    }
    .toast-close .material-symbols-outlined { font-size: 18px; }
    .toast-close:hover { color: var(--pf-ink); }
  `]
})
export class ToastContainerComponent {
  toasts$ = this.toastService.toasts$;

  constructor(private toastService: ToastService) {}

  icono(type: ToastType): string {
    switch (type) {
      case 'success': return 'check_circle';
      case 'error': return 'error';
      default: return 'info';
    }
  }

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
