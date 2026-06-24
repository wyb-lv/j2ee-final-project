export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  name: string;
  role: string;
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
