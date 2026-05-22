import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { timeout } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, ButtonModule, MessageModule, ProgressSpinnerModule, RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.scss',
})
export class VerifyEmail implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(true);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  private fallbackTimer?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.showError('Doğrulama bağlantısı geçersiz. Token bulunamadı.');
      return;
    }

    this.fallbackTimer = setTimeout(() => {
      if (this.isLoading()) {
        this.showError(
          'E-posta doğrulama isteği zaman aşımına uğradı. Lütfen bağlantıyı tekrar açın.',
        );
      }
    }, 10000);

    this.authService
      .verifyEmail(token)
      .pipe(timeout(8000))
      .subscribe({
        next: (response) => {
          this.clearFallbackTimer();
          this.isLoading.set(false);
          this.successMessage.set(response.message);
          this.errorMessage.set('');
        },
        error: (error: HttpErrorResponse | Error) => {
          this.clearFallbackTimer();
          const message =
            error instanceof HttpErrorResponse
              ? (error.error?.message ?? 'E-posta doğrulanamadı. Bağlantı süresi dolmuş olabilir.')
              : 'E-posta doğrulama isteği zaman aşımına uğradı. Lütfen bağlantıyı tekrar açın.';
          this.showError(message);
        },
      });
  }

  ngOnDestroy(): void {
    this.clearFallbackTimer();
  }

  private showError(message: string): void {
    this.isLoading.set(false);
    this.successMessage.set('');
    this.errorMessage.set(message);
  }

  private clearFallbackTimer(): void {
    if (this.fallbackTimer) {
      clearTimeout(this.fallbackTimer);
      this.fallbackTimer = undefined;
    }
  }
}
