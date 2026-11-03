import { apiClient } from './client';
import type { Clase, ClaseRequest } from '../types/clase';

export const claseService = {
  /**
   * Obtiene todas las clases (ADMIN o PROFESOR)
   * GET /api/clases
   */
  async getClases(): Promise<Clase[]> {
    const response = await apiClient.get<Clase[]>('/clases');
    return response.data;
  },

  /**
   * Obtiene las clases donde el usuario en sesión es el profesor titular
   * GET /api/clases/mis-clases
   */
  async getMisClases(): Promise<Clase[]> {
    const response = await apiClient.get<Clase[]>('/clases/mis-clases');
    return response.data;
  },

  /**
   * Obtiene las clases asociadas a una asignatura específica
   * GET /api/clases/asignatura/{asignaturaId}
   */
  async getClasesByAsignatura(asignaturaId: number): Promise<Clase[]> {
    const response = await apiClient.get<Clase[]>(`/clases/asignatura/${asignaturaId}`);
    return response.data;
  },

  /**
   * Obtiene las clases impartidas por un profesor específico
   * GET /api/clases/profesor/{profesorId}
   */
  async getClasesByProfesor(profesorId: number): Promise<Clase[]> {
    const response = await apiClient.get<Clase[]>(`/clases/profesor/${profesorId}`);
    return response.data;
  },

  /**
   * Obtiene una clase por su ID
   * GET /api/clases/{id}
   */
  async getClaseById(id: number): Promise<Clase> {
    const response = await apiClient.get<Clase>(`/clases/${id}`);
    return response.data;
  },

  /**
   * Crea una nueva clase asignando aula, grupo, asignatura y profesor titular (Solo ADMIN)
   * POST /api/clases
   */
  async createClase(data: ClaseRequest): Promise<Clase> {
    const response = await apiClient.post<Clase>('/clases', data);
    return response.data;
  },

  /**
   * Actualiza una clase existente (Solo ADMIN)
   * PUT /api/clases/{id}
   */
  async updateClase(id: number, data: ClaseRequest): Promise<Clase> {
    const response = await apiClient.put<Clase>(`/clases/${id}`, data);
    return response.data;
  },

  /**
   * Elimina una clase del sistema (Solo ADMIN)
   * DELETE /api/clases/{id}
   */
  async deleteClase(id: number): Promise<void> {
    await apiClient.delete(`/clases/${id}`);
  },
};

