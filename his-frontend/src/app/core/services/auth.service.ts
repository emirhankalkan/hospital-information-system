import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap, throwError } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  AuthUser,
  ForgotPasswordRequest,
  JwtResponse,
  LoginRequest,
  RefreshTokenRequest,
  RegisterRequest,
  ResendVerificationRequest,
  ResetPasswordRequest,
} from '../../models/auth.models';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly authUrl = `${environment.apiUrl}/auth`;

  readonly currentUser = this.tokenStorage.currentUser;
  readonly homeUrl = computed(() => this.resolveHomeUrl(this.currentUser()));

  isAuthenticated(): boolean {
    return this.tokenStorage.isAuthenticated();
  }

  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<ApiResponse<JwtResponse>>(`${this.authUrl}/login`, request).pipe(
      map((response) => response.data),
      tap((jwtResponse) => this.tokenStorage.saveSession(jwtResponse)),
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.authUrl}/register`, request);
  }

  /**
   * Access token'ı refresh token kullanarak yeniler.
   * Başarılıysa yeni session'ı storage'a kaydeder.
   * Refresh token yoksa session'ı temizler ve hata fırlatır.
   */
  refreshAccessToken(): Observable<JwtResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();

    if (!refreshToken) {
      this.tokenStorage.clearSession();
      return throwError(() => new Error('Refresh token bulunamadı.'));
    }

    const request: RefreshTokenRequest = { refreshToken };

    return this.http
      .post<ApiResponse<JwtResponse>>(`${this.authUrl}/refresh-token`, request)
      .pipe(
        map((response) => response.data),
        tap((jwtResponse) => this.tokenStorage.saveSession(jwtResponse)),
      );
  }

  /**
   * Çıkış yapar. Backend'e refresh token'ı revoke etmesi için istek gönderir,
   * ardından local session'ı temizler. HTTP hatası olsa dahi local temizlik yapılır.
   */
  logout(): void {
    const refreshToken = this.tokenStorage.getRefreshToken();

    if (!refreshToken) {
      this.tokenStorage.clearSession();
      return;
    }

    const request: RefreshTokenRequest = { refreshToken };

    this.http
      .post<ApiResponse<void>>(`${this.authUrl}/logout`, request)
      .pipe(finalize(() => this.tokenStorage.clearSession()))
      .subscribe({ error: () => {} }); // hata olsa da local temizlik finalize'da yapılır
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

  resendVerification(request: ResendVerificationRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.authUrl}/resend-verification`, request);
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
