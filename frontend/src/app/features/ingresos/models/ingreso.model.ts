export interface Ingreso {
  id: number;
  folio: string;
  placa: string;
  categoria: string;
  vehiculoId?: number;
  espacioId: number;
  espacioNumero?: string;
  tarifaId?: number;
  tarifaValor?: number;
  modalidad?: string;
  operador?: string;
  novedad?: string;
  estado: string;
  usuarioId?: number;
  fechaEntrada: string;
  fechaSalida?: string;
}

export interface IngresoRequest {
  placa: string;
  categoria: string;
  vehiculoId?: number;
  espacioId: number;
  espacioNumero?: string;
  tarifaId?: number;
  tarifaValor?: number;
  modalidad?: string;
  operador?: string;
  novedad?: string;
}
