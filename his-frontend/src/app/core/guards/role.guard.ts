import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { UserRole } from '../../models/auth.models';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data['roles'] ?? []) as UserRole[];
  const userRoles = authService.currentUser()?.roles ?? [];

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (allowedRoles.length === 0 || allowedRoles.some((role) => userRoles.includes(role))) {
    return true;
  }

  return router.createUrlTree([authService.homeUrl()]);
};
