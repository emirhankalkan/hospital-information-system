export type UserRole = 'ROLE_ADMIN' | 'ROLE_RECEPTIONIST' | 'ROLE_DOCTOR' | 'ROLE_PATIENT';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: string[];
  timestamp?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
}

export interface JwtResponse extends AuthUser {
  token: string;
  refreshToken: string;
  type: string;
}
