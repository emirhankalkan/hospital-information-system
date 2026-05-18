import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputTextModule, PasswordModule, ButtonModule, MessageModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})
export class ResetPassword implements OnInit {
  resetPasswordForm!: FormGroup;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.resetPasswordForm = this.fb.group(
      {
        token: [this.route.snapshot.queryParamMap.get('token') ?? '', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordsMatch },
    );
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';
    const { token, newPassword } = this.resetPasswordForm.value;

    this.authService.resetPassword({ token, newPassword }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message;
        this.resetPasswordForm.disable();
        setTimeout(() => this.router.navigateByUrl('/login'), 1500);
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message ?? 'Şifre güncellenemedi. Token bilgilerinizi kontrol edin.';
      },
    });
  }

  get passwordsDoNotMatch(): boolean {
    return Boolean(
      this.resetPasswordForm?.errors?.['passwordMismatch'] &&
        this.resetPasswordForm.get('confirmPassword')?.touched,
    );
  }

  private passwordsMatch(form: FormGroup): Record<string, boolean> | null {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    return newPassword && confirmPassword && newPassword !== confirmPassword ? { passwordMismatch: true } : null;
  }
}
