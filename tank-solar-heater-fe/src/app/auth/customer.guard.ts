import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Keeps admins out of the storefront. A signed-in admin hitting a shop page is
 * redirected to the admin console; everyone else is allowed through.
 */
export const customerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const user = auth.user();
  if (user && user.role?.toLowerCase() === 'admin') {
    return router.createUrlTree(['/admin']);
  }
  return true;
};
