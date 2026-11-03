import { apiClient } from './client';
import type { Asignatura, AsignaturaRequest } from '../types/asignatura';

export const asignaturaService = {
  /**
   * Obtiene todas las asignaturas
   * GET /api/asignaturas
   */
  async getAsignaturas(): Promise<Asignatura[]> {
    const response = await apiClient.get<Asignatura[]>('/asignaturas');
    return response.data;
  },

  /**
   * Obtiene las asignaturas pertenecientes a una formación específica
   * GET /api/asignaturas/formacion/{formacionId}
   */
  async getAsignaturasByFormacion(formacionId: number): Promise<Asignatura[]> {
    const response = await apiClient.get<Asignatura[]>(`/asignaturas/formacion/${formacionId}`);
    return response.data;
  },

  /**
   * Obtiene una asignatura por su ID
   * GET /api/asignaturas/{id}
   */
  async getAsignaturaById(id: number): Promise<Asignatura> {
    const response = await apiClient.get<Asignatura>(`/asignaturas/${id}`);
    return response.data;
  },

  /**
   * Registra una nueva asignatura asociada a una formación (Solo ADMIN)
   * POST /api/asignaturas
   */
  async createAsignatura(data: AsignaturaRequest): Promise<Asignatura> {
    const response = await apiClient.post<Asignatura>('/asignaturas', data);
    return response.data;
  },

  /**
   * Actualiza una asignatura existente (Solo ADMIN)
   * PUT /api/asignaturas/{id}
   */
  async updateAsignatura(id: number, data: AsignaturaRequest): Promise<Asignatura> {
    const response = await apiClient.put<Asignatura>(`/asignaturas/${id}`, data);
    return response.data;
  },

  /**
   * Elimina una asignatura (Solo ADMIN)
   * DELETE /api/asignaturas/{id}
   */
  async deleteAsignatura(id: number): Promise<void> {
    await apiClient.delete(`/asignaturas/${id}`);
  },
};

