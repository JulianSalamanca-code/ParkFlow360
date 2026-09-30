export interface Pago {
  id: number;
  vehiculoId: number;
  espacioId: number;
  tarifaId: number;
  valor: number;
  fecha: string;
  metodoPago: string;
}

export interface PagoRequest {
  vehiculoId: number;
  espacioId: number;
  tarifaId: number;
  valor: number;
  metodoPago: string;
}
