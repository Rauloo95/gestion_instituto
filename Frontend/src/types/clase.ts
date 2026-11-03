export interface Clase {
  id: number;
  nombre: string;
  grupo?: string;
  aula?: string;
  activo?: boolean;
  asignaturaId: number;
  asignaturaNombre?: string;
  profesorId: number;
  profesorNombre?: string;
}

export interface ClaseRequest {
  nombre: string;
  grupo?: string;
  aula?: string;
  asignaturaId: number;
  profesorId: number;
}

