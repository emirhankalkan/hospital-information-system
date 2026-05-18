import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
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
  imports: [CommonModule, ReactiveFormsModule, InputTextModule, PasswordModule, ButtonModule, MessageModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group(
      {
        fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordsMatch },
    );
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';
    const { fullName, email, password } = this.registerForm.value;

    this.authService.register({ fullName, username: this.createTechnicalUsername(email), email, password }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message;
        this.registerForm.disable();
        setTimeout(() => this.router.navigateByUrl('/login'), 1800);
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message ?? 'Kayıt oluşturulamadı. Bilgilerinizi kontrol edin.';
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

    return password && confirmPassword && password !== confirmPassword ? { passwordMismatch: true } : null;
  }

  private createTechnicalUsername(email: string): string {
    const emailPrefix = email.split('@')[0] ?? 'patient';
    const normalizedPrefix = emailPrefix.toLowerCase().replace(/[^a-z0-9._-]/g, '.').slice(0, 32);

    return `${normalizedPrefix || 'patient'}.${Date.now().toString(36)}`;
  }
}
