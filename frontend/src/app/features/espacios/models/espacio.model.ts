export interface Espacio {
  id: number;
  numero: string;
  tipo: string;
  estado: string;
  piso: number;
  fechaCreacion: string;
}

export interface EspacioRequest {
  numero: string;
  tipo: string;
  estado: string;
  piso: number;
}
