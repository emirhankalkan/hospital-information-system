import { Injectable, computed, signal } from '@angular/core';

import { AuthUser, JwtResponse } from '../../models/auth.models';

const ACCESS_TOKEN_KEY = 'his_access_token';
const REFRESH_TOKEN_KEY = 'his_refresh_token';
const USER_KEY = 'his_user';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly currentUserSignal = signal<AuthUser | null>(this.readUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.getAccessToken() && this.currentUserSignal()));

  saveSession(response: JwtResponse): void {
    const user: AuthUser = {
      id: response.id,
      username: response.username,
      email: response.email,
      roles: response.roles,
    };

    localStorage.setItem(ACCESS_TOKEN_KEY, response.token);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private readUser(): AuthUser | null {
    const rawUser = localStorage.getItem(USER_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as AuthUser;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
