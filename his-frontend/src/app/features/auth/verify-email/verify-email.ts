import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
export class VerifyEmail implements OnInit {
  isLoading = true;
  successMessage = '';
  errorMessage = '';
  private fallbackTimer?: ReturnType<typeof setTimeout>;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.showError('Doğrulama bağlantısı geçersiz. Token bulunamadı.');
      return;
    }

    this.fallbackTimer = setTimeout(() => {
      if (this.isLoading) {
        this.showError('E-posta doğrulama isteği zaman aşımına uğradı. Lütfen bağlantıyı tekrar açın.');
      }
    }, 10000);

    this.authService.verifyEmail(token).pipe(timeout(8000)).subscribe({
      next: (response) => {
        this.clearFallbackTimer();
        this.isLoading = false;
        this.successMessage = response.message;
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse | Error) => {
        this.clearFallbackTimer();
        const message =
          error instanceof HttpErrorResponse
            ? error.error?.message ?? 'E-posta doğrulanamadı. Bağlantı süresi dolmuş olabilir.'
            : 'E-posta doğrulama isteği zaman aşımına uğradı. Lütfen bağlantıyı tekrar açın.';
        this.showError(message);
      },
    });
  }

  private showError(message: string): void {
    this.isLoading = false;
    this.successMessage = '';
    this.errorMessage = message;
    this.cdr.detectChanges();
  }

  private clearFallbackTimer(): void {
    if (this.fallbackTimer) {
      clearTimeout(this.fallbackTimer);
      this.fallbackTimer = undefined;
    }
  }
}
