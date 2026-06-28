import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, shareReplay, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, RegisterRequest, TokenResponse } from './auth.models';

const TOKEN_KEY = 'suntank_token';
const REFRESH_KEY = 'suntank_refresh';
const USER_KEY = 'suntank_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private base = environment.apiBase;

  readonly user = signal<AuthUser | null>(this.restoreUser());
  readonly isLoggedIn = computed(() => this.user() !== null);

  /** Shared in-flight refresh so concurrent 401s trigger only one /auth/refresh call. */
  private refresh$: Observable<TokenResponse> | null = null;

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, payload).pipe(
      tap((res) => {
        const user: AuthUser = {
          id: res.id,
          name: res.name,
          role: res.role,
          email: payload.email,
        };
        this.store(res.token, res.refreshToken, user);
      })
    );
  }

  /** Registers, then logs in automatically with the same credentials. */
  register(payload: RegisterRequest): Observable<LoginResponse> {
    return this.http
      .post(`${this.base}/auth/register`, payload)
      .pipe(switchMap(() => this.login({ email: payload.email, password: payload.password })));
  }

  logout(): void {
    // Invalidate the tokens server-side (best-effort) while the access token is still
    // attached by the interceptor, then drop the client's copy. The local clear must
    // stay — the server can't erase the browser's storage.
    this.http.post(`${this.base}/auth/logout`, {}).subscribe({ error: () => {} });
    this.clearSession();
  }

  /**
   * Swaps the stored refresh token for a fresh access + refresh pair. Concurrent callers
   * share a single request. On failure the caller is responsible for handling the error.
   */
  refresh(): Observable<TokenResponse> {
    const refreshToken = this.refreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token'));
    }
    if (this.refresh$) {
      return this.refresh$;
    }
    this.refresh$ = this.http
      .post<TokenResponse>(`${this.base}/auth/refresh`, { refreshToken })
      .pipe(
        tap((res) => this.storeTokens(res.token, res.refreshToken)),
        finalize(() => (this.refresh$ = null)),
        shareReplay(1)
      );
    return this.refresh$;
  }

  token(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  refreshToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(REFRESH_KEY);
  }

  /** Clears all client-side auth state (used on logout and on an unrecoverable refresh). */
  clearSession(): void {
    this.user.set(null);
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  }

  private store(token: string, refreshToken: string, user: AuthUser): void {
    this.user.set(user);
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.storeTokens(token, refreshToken);
  }

  private storeTokens(token: string, refreshToken: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(REFRESH_KEY, refreshToken);
  }

  private restoreUser(): AuthUser | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as AuthUser) : null;
    } catch {
      return null;
    }
  }
}
