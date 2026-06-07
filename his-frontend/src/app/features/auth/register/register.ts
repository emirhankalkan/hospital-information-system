import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    RouterLink,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  readonly registerForm: FormGroup = this.fb.group(
    {
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: this.passwordsMatch },
  );

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');
    const { firstName, lastName, email, password } = this.registerForm.value;

    this.authService
      .register({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        username: this.createTechnicalUsername(email),
        email,
        password,
      })
      .subscribe({
        next: (response) => {
          this.isLoading.set(false);
          this.successMessage.set(response.message);
          this.registerForm.disable();
          setTimeout(() => this.router.navigateByUrl('/login'), 1800);
        },
        error: (error: HttpErrorResponse) => {
          this.isLoading.set(false);
          this.errorMessage.set(
            error.error?.message ?? 'Kayıt oluşturulamadı. Bilgilerinizi kontrol edin.',
          );
        },
      });
  }

  get passwordsDoNotMatch(): boolean {
    return Boolean(
      this.registerForm?.errors?.['passwordMismatch'] &&
        this.registerForm.get('confirmPassword')?.touched,
    );
  }

  private passwordsMatch(form: FormGroup): Record<string, boolean> | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password && confirmPassword && password !== confirmPassword
      ? { passwordMismatch: true }
      : null;
  }

  private createTechnicalUsername(email: string): string {
    const emailPrefix = email.split('@')[0] ?? 'patient';
    const normalizedPrefix = emailPrefix
      .toLowerCase()
      .replace(/[^a-z0-9._-]/g, '.')
      .slice(0, 32);
    return `${normalizedPrefix || 'patient'}.${Date.now().toString(36)}`;
  }
}
