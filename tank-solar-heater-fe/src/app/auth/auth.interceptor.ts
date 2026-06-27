import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/** Attaches the JWT to API requests so the backend knows the signed-in user. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  // Login/register have no token yet; everything else (incl. /auth/logout) gets it.
  const isPreAuthRoute = req.url.includes('/auth/login') || req.url.includes('/auth/register');

  if (token && req.url.startsWith(environment.apiBase) && !isPreAuthRoute) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
