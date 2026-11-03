export interface Asignatura {
  id: number;
  nombre: string;
  descripcion?: string;
  codigo?: string;
  curso?: string;
  formacionId: number;
  formacionNombre?: string;
}

export interface AsignaturaRequest {
  nombre: string;
  descripcion?: string;
  codigo?: string;
  curso?: string;
  formacionId: number;
}

