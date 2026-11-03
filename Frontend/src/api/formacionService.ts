import { apiClient } from './client';
import type { Formacion, FormacionRequest } from '../types/formacion';

export const formacionService = {
  /**
   * Obtiene todas las formaciones académicas
   * GET /api/formaciones
   */
  async getFormaciones(): Promise<Formacion[]> {
    const response = await apiClient.get<Formacion[]>('/formaciones');
    return response.data;
  },

  /**
   * Obtiene las formaciones impartidas en un centro específico
   * GET /api/formaciones/centro/{centroId}
   */
  async getFormacionesByCentro(centroId: number): Promise<Formacion[]> {
    const response = await apiClient.get<Formacion[]>(`/formaciones/centro/${centroId}`);
    return response.data;
  },

  /**
   * Obtiene una formación por ID
   * GET /api/formaciones/{id}
   */
  async getFormacionById(id: number): Promise<Formacion> {
    const response = await apiClient.get<Formacion>(`/formaciones/${id}`);
    return response.data;
  },

  /**
   * Registra una nueva formación ligada a un centro (Solo ADMIN)
   * POST /api/formaciones
   */
  async createFormacion(data: FormacionRequest): Promise<Formacion> {
    const response = await apiClient.post<Formacion>('/formaciones', data);
    return response.data;
  },

  /**
   * Actualiza una formación existente (Solo ADMIN)
   * PUT /api/formaciones/{id}
   */
  async updateFormacion(id: number, data: FormacionRequest): Promise<Formacion> {
    const response = await apiClient.put<Formacion>(`/formaciones/${id}`, data);
    return response.data;
  },

  /**
   * Elimina una formación (Solo ADMIN)
   * DELETE /api/formaciones/{id}
   */
  async deleteFormacion(id: number): Promise<void> {
    await apiClient.delete(`/formaciones/${id}`);
  },
};

