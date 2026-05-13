import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse, AuthUser, JwtResponse, LoginRequest } from '../../models/auth.models';
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
