import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  AuthUser,
  ForgotPasswordRequest,
  JwtResponse,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
} from '../../models/auth.models';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly authUrl = `${environment.apiUrl}/auth`;

  readonly currentUser = this.tokenStorage.currentUser;
  readonly isAuthenticated = this.tokenStorage.isAuthenticated;
  readonly homeUrl = computed(() => this.resolveHomeUrl(this.currentUser()));

  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<ApiResponse<JwtResponse>>(`${this.authUrl}/login`, request).pipe(
      map((response) => response.data),
      tap((jwtResponse) => this.tokenStorage.saveSession(jwtResponse)),
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.authUrl}/register`, request);
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.authUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.authUrl}/reset-password`, request);
  }

  verifyEmail(token: string): Observable<ApiResponse<void>> {
    return this.http.get<ApiResponse<void>>(`${this.authUrl}/verify-email`, {
      params: { token },
    });
  }

  logout(): void {
    this.tokenStorage.clearSession();
  }

  private resolveHomeUrl(user: AuthUser | null): string {
    const roles = user?.roles ?? [];

    if (roles.includes('ROLE_ADMIN')) {
      return '/admin';
    }
    if (roles.includes('ROLE_RECEPTIONIST')) {
      return '/receptionist';
    }
    if (roles.includes('ROLE_DOCTOR')) {
      return '/doctor';
    }
    if (roles.includes('ROLE_PATIENT')) {
      return '/patient';
    }

    return '/login';
  }
}
