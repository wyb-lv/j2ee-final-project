export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  id: number;
  name: string;
  role: string;
}

/** Returned by /auth/refresh: a new access token plus a rotated refresh token. */
export interface TokenResponse {
  token: string;
  refreshToken: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
}

/** The signed-in user we keep on the client. */
export interface AuthUser {
  id: number;
  name: string;
  role: string;
  email: string;
}
