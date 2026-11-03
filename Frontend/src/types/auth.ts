export type Rol = 'ADMIN' | 'PROFESOR' | 'ALUMNO';

export interface Usuario {
  id: number;
  nombre: string;
  apellidos?: string;
  email: string;
  rol: Rol;
}

export interface AuthResponse {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  apellidos: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  errors?: Record<string, string>;
}
