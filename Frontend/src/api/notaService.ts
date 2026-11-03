import { apiClient } from './client';
import type { Nota, NotaRequest } from '../types/nota';

export const notaService = {
  /**
   * Obtiene todas las notas del sistema (ADMIN y PROFESOR)
   * GET /api/notas
   */
  async getNotas(): Promise<Nota[]> {
    const response = await apiClient.get<Nota[]>('/notas');
    return response.data;
  },

  /**
   * Obtiene el expediente de notas del alumno en sesión
   * GET /api/notas/mis-notas
   */
  async getMisNotas(): Promise<Nota[]> {
    const response = await apiClient.get<Nota[]>('/notas/mis-notas');
    return response.data;
  },

  /**
   * Obtiene una calificación por su ID
   * GET /api/notas/{id}
   */
  async getNotaById(id: number): Promise<Nota> {
    const response = await apiClient.get<Nota>(`/notas/${id}`);
    return response.data;
  },

  /**
   * Obtiene la nota asociada a una matrícula
   * GET /api/notas/matricula/{matriculaId}
   */
  async getNotaByMatricula(matriculaId: number): Promise<Nota> {
    const response = await apiClient.get<Nota>(`/notas/matricula/${matriculaId}`);
    return response.data;
  },

  /**
   * Obtiene las notas de un alumno en concreto
   * GET /api/notas/alumno/{alumnoId}
   */
  async getNotasByAlumno(alumnoId: number): Promise<Nota[]> {
    const response = await apiClient.get<Nota[]>(`/notas/alumno/${alumnoId}`);
    return response.data;
  },

  /**
   * Obtiene las notas emitidas por un profesor específico
   * GET /api/notas/profesor/{profesorId}
   */
  async getNotasByProfesor(profesorId: number): Promise<Nota[]> {
    const response = await apiClient.get<Nota[]>(`/notas/profesor/${profesorId}`);
    return response.data;
  },

  /**
   * Califica a un alumno registrando una nota para su matrícula (Solo PROFESOR)
   * POST /api/notas
   */
  async createNota(data: NotaRequest): Promise<Nota> {
    const response = await apiClient.post<Nota>('/notas', data);
    return response.data;
  },

  /**
   * Modifica una calificación existente (Solo PROFESOR titular)
   * PUT /api/notas/{id}
   */
  async updateNota(id: number, data: NotaRequest): Promise<Nota> {
    const response = await apiClient.put<Nota>(`/notas/${id}`, data);
    return response.data;
  },

  /**
   * Elimina una calificación (Solo PROFESOR titular)
   * DELETE /api/notas/{id}
   */
  async deleteNota(id: number): Promise<void> {
    await apiClient.delete(`/notas/${id}`);
  },
};

