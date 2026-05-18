export type UserRole = 'ROLE_ADMIN' | 'ROLE_RECEPTIONIST' | 'ROLE_DOCTOR' | 'ROLE_PATIENT';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: string[];
  timestamp?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  username: string;
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
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
