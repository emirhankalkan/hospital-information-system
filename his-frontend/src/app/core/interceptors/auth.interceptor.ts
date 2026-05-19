import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, throwError } from 'rxjs';
import { catchError, filter, finalize, switchMap, take } from 'rxjs/operators';

import { JwtResponse } from '../../models/auth.models';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

/**
 * Modül seviyesinde durum değişkenleri.
 * Birden fazla eş zamanlı isteğin aynı anda token yenilemesini önler;
 * yenileme bitene kadar diğer istekler refreshTokenSubject'i bekler.
 */
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const PUBLIC_AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/forgot-password',
  '/auth/reset-password',
  '/auth/verify-email',
  '/auth/resend-verification',
  '/auth/refresh-token',
  '/auth/logout',
];

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(TokenStorageService);
  const authService = inject(AuthService);
  const router = inject(Router);

  const isPublicEndpoint = PUBLIC_AUTH_ENDPOINTS.some((endpoint) =>
    request.url.includes(endpoint),
  );

  // Public endpoint'lere token ekleme ve 401 handling yapma
  if (isPublicEndpoint) {
    return next(request);
  }

  const token = tokenStorage.getAccessToken();
  const authorizedRequest = addBearerToken(request, token);

  return next(authorizedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        return handle401Error(request, next, tokenStorage, authService, router);
      }
      return throwError(() => error);
    }),
  );
};

/**
 * 401 durumunda token yenileme akışı.
 * - isRefreshing false ise: Yenileme isteği başlatılır, yeni token ile orijinal istek tekrarlanır.
 * - isRefreshing true ise: Yeni token gelene kadar bekleyip orijinal isteği tekrarlar.
 */
function handle401Error(
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  tokenStorage: TokenStorageService,
  authService: AuthService,
  router: Router,
) {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshAccessToken().pipe(
      switchMap((jwtResponse: JwtResponse) => {
        refreshTokenSubject.next(jwtResponse.token);
        return next(addBearerToken(request, jwtResponse.token));
      }),
      catchError((err) => {
        // Refresh token da geçersiz → session temizle ve login'e yönlendir
        authService.logout();
        router.navigate(['/login']);
        return throwError(() => err);
      }),
      finalize(() => {
        isRefreshing = false;
      }),
    );
  }

  // Token yenileme zaten sürüyor — yeni token gelene kadar bekle
  return refreshTokenSubject.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(addBearerToken(request, token))),
  );
}

function addBearerToken(request: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  if (!token) return request;
  return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}
