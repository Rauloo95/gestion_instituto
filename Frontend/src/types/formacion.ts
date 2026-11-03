export interface Formacion {
  id: number;
  nombre: string;
  descripcion?: string;
  nivel?: string;
  codigo?: string;
  centroId: number;
  centroNombre?: string;
}

export interface FormacionRequest {
  nombre: string;
  descripcion?: string;
  nivel?: string;
  codigo?: string;
  centroId: number;
}

