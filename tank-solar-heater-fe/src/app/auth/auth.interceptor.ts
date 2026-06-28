import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/** Routes that must never carry an access token (they run before/around auth). */
function isPreAuthRoute(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh')
  );
}

/**
 * Attaches the JWT to API requests and, on a 401, transparently refreshes the access
 * token once and replays the request.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isApi = req.url.startsWith(environment.apiBase);
  const eligible = isApi && !isPreAuthRoute(req.url);

  const token = auth.token();
  const authReq =
    token && eligible
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // Only attempt a refresh for an authenticated API call that came back 401.
      if (err.status !== 401 || !eligible || !auth.refreshToken()) {
        return throwError(() => err);
      }

      return auth.refresh().pipe(
        switchMap((res) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${res.token}` } }))
        ),
        catchError((refreshErr) => {
          // Refresh itself failed — the session is unrecoverable; force a clean logout.
          auth.clearSession();
          return throwError(() => refreshErr);
        })
      );
    })
  );
};
