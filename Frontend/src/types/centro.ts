export interface Centro {
  id: number;
  nombre: string;
  tipo: string;
  direccion?: string;
  ciudad?: string;
  codigoPostal?: string;
  telefono?: string;
  email?: string;
  activo?: boolean;
}

export interface CentroRequest {
  nombre: string;
  tipo: string;
  direccion?: string;
  ciudad?: string;
  codigoPostal?: string;
  telefono?: string;
  email?: string;
}

