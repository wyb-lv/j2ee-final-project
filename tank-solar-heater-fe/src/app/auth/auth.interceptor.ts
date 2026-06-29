import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/** Routes that must never carry a session token (they run before/around auth). */
function isPreAuthRoute(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh')
  );
}

/**
 * Attaches the opaque session id as a Bearer token to API requests and, on a 401, transparently
 * refreshes the session once and replays the request. The server resolves the session id to the
 * real JWT in Redis — the JWT is never seen or stored by the client.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isApi = req.url.startsWith(environment.apiBase);
  const eligible = isApi && !isPreAuthRoute(req.url);

  const sessionId = auth.sessionId();
  const authReq =
    sessionId && eligible
      ? req.clone({ setHeaders: { Authorization: `Bearer ${sessionId}` } })
      : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // Only attempt a refresh for an authenticated API call that came back 401.
      if (err.status !== 401 || !eligible || !auth.refreshToken()) {
        return throwError(() => err);
      }

      return auth.refresh().pipe(
        switchMap((res) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${res.sessionId}` } }))
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
