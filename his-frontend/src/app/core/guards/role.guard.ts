import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { UserRole } from '../../models/auth.models';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data['roles'] ?? []) as UserRole[];
  if (!authService.isAuthenticated()) {
    return authService.refreshAccessToken().pipe(
      map(() => resolveRoleAccess(authService, router, allowedRoles)),
      catchError(() => of(router.createUrlTree(['/login']))),
    );
  }

  return resolveRoleAccess(authService, router, allowedRoles);
};

function resolveRoleAccess(authService: AuthService, router: Router, allowedRoles: UserRole[]) {
  const userRoles = authService.currentUser()?.roles ?? [];

  if (allowedRoles.length === 0 || allowedRoles.some((role) => userRoles.includes(role))) {
    return true;
  }

  return router.createUrlTree([authService.homeUrl()]);
}
