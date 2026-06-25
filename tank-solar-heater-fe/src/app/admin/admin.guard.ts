import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/** Allows the route only for signed-in admins. */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const user = auth.user();
  if (user && user.role?.toLowerCase() === 'admin') return true;

  if (!user) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  // Logged in but not an admin → bounce to the storefront.
  return router.createUrlTree(['/']);
};
