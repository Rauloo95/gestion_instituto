export type EstadoMatricula = 'ACTIVA' | 'CANCELADA' | 'FINALIZADA';

export interface Matricula {
  id: number;
  alumnoId: number;
  alumnoNombre: string;
  claseId: number;
  claseNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  fechaMatricula: string;
  estado: EstadoMatricula;
}

export interface MatriculaRequest {
  alumnoId: number;
  claseId: number;
  estado?: EstadoMatricula;
}

