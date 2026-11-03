import { apiClient } from './client';
import type { UsuarioResponse, UsuarioRequest } from '../types/usuario';

export const usuarioService = {
  /**
   * Obtiene todos los usuarios registrados
   * GET /api/usuarios
   */
  async getUsuarios(): Promise<UsuarioResponse[]> {
    const response = await apiClient.get<UsuarioResponse[]>('/usuarios');
    return response.data;
  },

  /**
   * Obtiene únicamente los usuarios con rol PROFESOR
   */
  async getProfesores(): Promise<UsuarioResponse[]> {
    const usuarios = await this.getUsuarios();
    return usuarios.filter((u) => u.rol === 'PROFESOR');
  },

  /**
   * Obtiene un usuario por ID
   * GET /api/usuarios/{id}
   */
  async getUsuarioById(id: number): Promise<UsuarioResponse> {
    const response = await apiClient.get<UsuarioResponse>(`/usuarios/${id}`);
    return response.data;
  },

  /**
   * Crea un nuevo usuario en el sistema (Solo ADMIN)
   * POST /api/usuarios
   */
  async createUsuario(data: UsuarioRequest): Promise<UsuarioResponse> {
    const response = await apiClient.post<UsuarioResponse>('/usuarios', data);
    return response.data;
  },

  /**
   * Actualiza los datos de un usuario (Solo ADMIN)
   * PUT /api/usuarios/{id}
   */
  async updateUsuario(id: number, data: UsuarioRequest): Promise<UsuarioResponse> {
    const response = await apiClient.put<UsuarioResponse>(`/usuarios/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un usuario (Solo ADMIN)
   * DELETE /api/usuarios/{id}
   */
  async deleteUsuario(id: number): Promise<void> {
    await apiClient.delete(`/usuarios/${id}`);
  },
};

