export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Returned by /auth/login. The JWT stays server-side in Redis; the client only receives an
 * opaque `sessionId` it sends as a Bearer token, plus a refresh token and the UI profile.
 */
export interface LoginResponse {
  sessionId: string;
  refreshToken: string;
  id: number;
  name: string;
  role: string;
}

/** Returned by /auth/refresh: a new session id plus a rotated refresh token. */
export interface TokenResponse {
  sessionId: string;
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
