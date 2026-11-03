import type { Rol } from './auth';

export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  rol: Rol;
}

export interface UsuarioRequest {
  nombre: string;
  apellidos: string;
  email: string;
  password: string;
  rol: Rol;
}
