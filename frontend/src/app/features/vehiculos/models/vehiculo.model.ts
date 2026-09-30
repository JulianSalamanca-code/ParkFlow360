export interface Vehiculo {
  id: number;
  placa: string;
  tipo: string;
  color: string;
  modelo: string;
  fechaCreacion: string;
}

export interface VehiculoRequest {
  placa: string;
  tipo: string;
  color: string;
  modelo: string;
}
