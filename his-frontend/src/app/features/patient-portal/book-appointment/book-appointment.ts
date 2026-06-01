import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-book-appointment',
  standalone: true,
  imports: [ButtonModule, RouterLink],
  template: `
    <main class="book-page">
      <div class="book-shell">
        <header class="book-header">
          <div>
            <p class="eyebrow">Hasta Portalı</p>
            <h1>Randevu Al</h1>
            <p>Bu özellik yakında eklenecek.</p>
          </div>
          <div class="book-header-actions">
            <p-button label="Panele Dön" icon="pi pi-arrow-left" severity="secondary" routerLink="/patient" />
            <p-button label="Çıkış Yap" icon="pi pi-power-off" severity="secondary" (onClick)="logout()" />
          </div>
        </header>

        <div class="book-placeholder">
          <span class="pi pi-calendar-clock"></span>
          <h2>Randevu Alma Sistemi</h2>
          <p>Bölüm seçimi, doktor listesi ve uygun saat seçimi bu sayfada yer alacak.</p>
          <p-button label="Panele Dön" icon="pi pi-arrow-left" routerLink="/patient" />
        </div>
      </div>
    </main>
  `,
  styles: [`
    .book-page {
      min-height: 100vh;
      background: #f6f8fb;
      padding: 1.5rem;
    }
    .book-shell {
      max-width: 980px;
      margin: 0 auto;
    }
    .book-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.5rem;
      border: 1px solid #dbe3ee;
      border-radius: 8px;
      background: #fff;
      padding: 1.25rem;
      box-shadow: 0 8px 24px rgba(15,23,42,0.09);
      h1 { margin: 0.2rem 0; color: #0f172a; font-size: 1.85rem; font-weight: 800; }
      p { margin: 0; color: #526173; font-weight: 600; }
    }
    .book-header-actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      align-items: center;
    }
    .eyebrow {
      color: #036672;
      font-size: 0.75rem;
      font-weight: 800;
      text-transform: uppercase;
      margin: 0;
    }
    .book-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      min-height: 320px;
      border: 2px dashed #cbd5e1;
      border-radius: 12px;
      background: #fff;
      text-align: center;
      padding: 2rem;
      .pi { font-size: 3rem; color: #036672; }
      h2 { margin: 0; color: #101828; font-size: 1.3rem; font-weight: 800; }
      p { margin: 0; color: #667085; max-width: 400px; line-height: 1.6; }
    }
  `],
})
export class BookAppointment {
  private readonly authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}
