import { apiClient } from './client';
import type { Matricula, MatriculaRequest } from '../types/matricula';

export const matriculaService = {
  /**
   * Obtiene todas las matrículas registradas
   * GET /api/matriculas
   */
  async getMatriculas(): Promise<Matricula[]> {
    const response = await apiClient.get<Matricula[]>('/matriculas');
    return response.data;
  },

  /**
   * Obtiene una matrícula por ID
   * GET /api/matriculas/{id}
   */
  async getMatriculaById(id: number): Promise<Matricula> {
    const response = await apiClient.get<Matricula>(`/matriculas/${id}`);
    return response.data;
  },

  /**
   * Obtiene las matrículas de un alumno en específico
   * GET /api/matriculas/alumno/{alumnoId}
   */
  async getMatriculasByAlumno(alumnoId: number): Promise<Matricula[]> {
    const response = await apiClient.get<Matricula[]>(`/matriculas/alumno/${alumnoId}`);
    return response.data;
  },

  /**
   * Obtiene los alumnos matriculados en una clase
   * GET /api/matriculas/clase/{claseId}
   */
  async getMatriculasByClase(claseId: number): Promise<Matricula[]> {
    const response = await apiClient.get<Matricula[]>(`/matriculas/clase/${claseId}`);
    return response.data;
  },

  /**
   * Registra una nueva matrícula para un alumno en una clase (Solo ADMIN)
   * POST /api/matriculas
   */
  async createMatricula(data: MatriculaRequest): Promise<Matricula> {
    const response = await apiClient.post<Matricula>('/matriculas', data);
    return response.data;
  },

  /**
   * Actualiza el estado o datos de una matrícula existente (Solo ADMIN)
   * PUT /api/matriculas/{id}
   */
  async updateMatricula(id: number, data: MatriculaRequest): Promise<Matricula> {
    const response = await apiClient.put<Matricula>(`/matriculas/${id}`, data);
    return response.data;
  },

  /**
   * Elimina una matrícula (Solo ADMIN)
   * DELETE /api/matriculas/{id}
   */
  async deleteMatricula(id: number): Promise<void> {
    await apiClient.delete(`/matriculas/${id}`);
  },
};

