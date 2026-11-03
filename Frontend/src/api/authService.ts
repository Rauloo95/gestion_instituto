import { apiClient } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest, Usuario } from '../types/auth';

export const authService = {
  /**
   * Inicia sesión en la API de Gestión Baleras
   * POST /api/auth/login
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  /**
   * Registra un nuevo alumno
   * POST /api/auth/register
   */
  async register(data: RegisterRequest): Promise<Usuario> {
    const response = await apiClient.post<Usuario>('/auth/register', data);
    return response.data;
  },
};

