import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DividerModule } from 'primeng/divider';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';

import { AuthService } from '../../../core/services/auth.service';

type DemoSeverity = 'danger' | 'success' | 'warning' | 'info';

interface DemoAccount {
  label: string;
  role: string;
  email: string;
  icon: string;
  severity: DemoSeverity;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    CheckboxModule,
    ButtonModule,
    MessageModule,
    DividerModule,
    RouterLink,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(false);
  readonly errorMessage = signal('');

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rememberMe: [false],
  });

  readonly demoAccounts: DemoAccount[] = [
    {
      label: 'Admin',
      role: 'Sistem Yöneticisi',
      email: 'admin@his.local',
      icon: 'pi pi-shield',
      severity: 'danger',
    },
    {
      label: 'Doktor',
      role: 'Dr. Aylin Kaya',
      email: 'aylin.kaya@his.local',
      icon: 'pi pi-heart',
      severity: 'success',
    },
    {
      label: 'Resepsiyon',
      role: 'Resepsiyonist',
      email: 'receptionist@his.local',
      icon: 'pi pi-desktop',
      severity: 'warning',
    },
    {
      label: 'Hasta',
      role: 'Elif Yılmaz',
      email: 'elif.yilmaz@example.com',
      icon: 'pi pi-user',
      severity: 'info',
    },
  ];

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    const { email, password } = this.loginForm.value;

    this.authService.login({ email: email!, password: password! }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigateByUrl(this.authService.homeUrl());
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          error.error?.message ?? 'Giriş yapılamadı. Bilgilerinizi kontrol edin.',
        );
      },
    });
  }

  /** Demo hesaba tıklanınca formu doldur ve otomatik giriş yap */
  quickLogin(email: string): void {
    this.loginForm.patchValue({ email, password: 'Password123!' });
    this.onSubmit();
  }
}
