export interface Nota {
  id: number;
  valor: number;
  observaciones?: string;
  fechaEvaluacion?: string;
  matriculaId: number;
  alumnoId: number;
  alumnoNombre: string;
  claseId: number;
  claseNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  profesorId: number;
  profesorNombre: string;
}

export interface NotaRequest {
  valor: number;
  observaciones?: string;
  matriculaId: number;
}

