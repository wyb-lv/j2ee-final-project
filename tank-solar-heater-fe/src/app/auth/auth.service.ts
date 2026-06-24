import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, RegisterRequest } from './auth.models';

const TOKEN_KEY = 'suntank_token';
const USER_KEY = 'suntank_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private base = environment.apiBase;

  readonly user = signal<AuthUser | null>(this.restoreUser());
  readonly isLoggedIn = computed(() => this.user() !== null);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, payload).pipe(
      tap((res) => {
        const user: AuthUser = {
          id: res.id,
          name: res.name,
          role: res.role,
          email: payload.email,
        };
        this.store(res.token, user);
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
    this.user.set(null);
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  token(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  private store(token: string, user: AuthUser): void {
    this.user.set(user);
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
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
