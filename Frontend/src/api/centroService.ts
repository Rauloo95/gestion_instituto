import { apiClient } from './client';
import type { Centro, CentroRequest } from '../types/centro';

export const centroService = {
  /**
   * Obtiene todos los centros educativos
   * GET /api/centros
   */
  async getCentros(): Promise<Centro[]> {
    const response = await apiClient.get<Centro[]>('/centros');
    return response.data;
  },

  /**
   * Obtiene un centro por ID
   * GET /api/centros/{id}
   */
  async getCentroById(id: number): Promise<Centro> {
    const response = await apiClient.get<Centro>(`/centros/${id}`);
    return response.data;
  },

  /**
   * Crea un nuevo centro educativo (Solo ADMIN)
   * POST /api/centros
   */
  async createCentro(data: CentroRequest): Promise<Centro> {
    const response = await apiClient.post<Centro>('/centros', data);
    return response.data;
  },

  /**
   * Actualiza los datos de un centro (Solo ADMIN)
   * PUT /api/centros/{id}
   */
  async updateCentro(id: number, data: CentroRequest): Promise<Centro> {
    const response = await apiClient.put<Centro>(`/centros/${id}`, data);
    return response.data;
  },

  /**
   * Elimina un centro (Solo ADMIN)
   * DELETE /api/centros/{id}
   */
  async deleteCentro(id: number): Promise<void> {
    await apiClient.delete(`/centros/${id}`);
  },
};

