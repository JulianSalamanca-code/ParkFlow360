export interface Tarifa {
  id: number;
  nombre: string;
  tipo: string;
  valor: number;
  duracion: string;
  fechaCreacion: string;
}

export interface TarifaRequest {
  nombre: string;
  tipo: string;
  valor: number;
  duracion: string;
}
